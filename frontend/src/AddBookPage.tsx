import { useState } from 'react'
import { isValidIsbn, lookupIsbn, checkIsbnExists } from './isbnLookup'

type LookupState = 'idle' | 'loading' | 'resolved' | 'not-found' | 'error' | 'already-exists'
type SubmitState = 'idle' | 'submitting' | 'success' | 'error'

interface AddBookPageProps {
  onBack: () => void
}

function AddBookPage({ onBack }: AddBookPageProps) {
  const [isbn, setIsbn] = useState('')
  const [lookupState, setLookupState] = useState<LookupState>('idle')
  const [title, setTitle] = useState('')
  const [authors, setAuthors] = useState('')
  const [publicationYear, setPublicationYear] = useState('')
  const [submitState, setSubmitState] = useState<SubmitState>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const isbnTouched = isbn.length > 0
  const isbnValid = isValidIsbn(isbn)
  const showFields = lookupState === 'resolved'
  const canConfirm =
    showFields &&
    submitState !== 'submitting' &&
    title.trim().length > 0 &&
    authors.trim().length > 0

  const handleLookup = async () => {
    if (!isbnValid) return

    setLookupState('loading')
    setErrorMessage('')
    setSuccessMessage('')
    setSubmitState('idle')

    try {
      const exists = await checkIsbnExists(isbn)
      if (exists) {
        setLookupState('already-exists')
        setErrorMessage('This tome already resides within the Great Library')
        return
      }

      const result = await lookupIsbn(isbn)
      if (!result) {
        setLookupState('not-found')
        setErrorMessage('No record of this tome exists in the known realms')
        return
      }

      setTitle(result.title)
      setAuthors(result.authors.join(', '))
      setPublicationYear(result.publicationYear != null ? String(result.publicationYear) : '')
      setLookupState('resolved')
    } catch {
      setLookupState('error')
      setErrorMessage('The palantir is clouded — the lookup service cannot be reached')
    }
  }

  const handleConfirm = async () => {
    if (!canConfirm) return

    setSubmitState('submitting')
    setErrorMessage('')

    try {
      const response = await fetch('http://localhost:8080/api/catalog/books', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          isbn,
          title: title.trim(),
          authors: authors.split(',').map((a) => a.trim()).filter(Boolean),
          publicationYear: publicationYear ? parseInt(publicationYear, 10) : 0,
        }),
      })

      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.error || 'Failed to add book')
      }

      setSubmitState('success')
      setSuccessMessage('The tome has been inscribed into the Great Library')
    } catch (err) {
      setSubmitState('error')
      setErrorMessage(err instanceof Error ? err.message : 'Failed to add book')
    }
  }

  const handleReset = () => {
    setIsbn('')
    setLookupState('idle')
    setTitle('')
    setAuthors('')
    setPublicationYear('')
    setSubmitState('idle')
    setErrorMessage('')
    setSuccessMessage('')
  }

  return (
    <div className="add-book-page">
      <button onClick={onBack} className="back-button">
        Return to the Archives
      </button>
      <h2>Inscribe a Tome by ISBN</h2>

      {submitState === 'success' ? (
        <div className="success-message">
          <p>{successMessage}</p>
          <div className="success-actions">
            <button onClick={handleReset} className="search-button">
              Inscribe Another
            </button>
            <button onClick={onBack} className="browse-button">
              Return to the Archives
            </button>
          </div>
        </div>
      ) : (
        <>
          <div className="lookup-form">
            <div className="form-field">
              <label htmlFor="isbn-input">ISBN</label>
              <div className="isbn-input-row">
                <input
                  id="isbn-input"
                  type="text"
                  value={isbn}
                  onChange={(e) => setIsbn(e.target.value)}
                  placeholder="Enter the 13-digit ISBN rune"
                  className="search-input"
                  disabled={lookupState === 'loading'}
                />
                <button
                  onClick={handleLookup}
                  disabled={!isbnValid || lookupState === 'loading'}
                  className="search-button"
                >
                  {lookupState === 'loading' ? 'Consulting the scribes...' : 'Consult'}
                </button>
              </div>
              {isbnTouched && !isbnValid && (
                <p className="validation-message">The runes are malformed — check the ISBN</p>
              )}
            </div>
          </div>

          {errorMessage && <p className="error">{errorMessage}</p>}

          {showFields && (
            <div className="book-form">
              <div className="form-field">
                <label htmlFor="title-input">Title of the Work</label>
                <input
                  id="title-input"
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="search-input"
                />
              </div>
              <div className="form-field">
                <label htmlFor="authors-input">Scribes & Authors</label>
                <input
                  id="authors-input"
                  type="text"
                  value={authors}
                  onChange={(e) => setAuthors(e.target.value)}
                  placeholder="Comma-separated"
                  className="search-input"
                />
              </div>
              <div className="form-field">
                <label htmlFor="year-input">Year of Publication</label>
                <input
                  id="year-input"
                  type="text"
                  value={publicationYear}
                  onChange={(e) => setPublicationYear(e.target.value)}
                  className="search-input"
                />
                {!publicationYear && (
                  <p className="warning-message">The year of this work's creation is lost to time</p>
                )}
              </div>
              <button
                onClick={handleConfirm}
                disabled={!canConfirm}
                className="confirm-button"
              >
                {submitState === 'submitting' ? 'The scribes are writing...' : 'Inscribe into the Archive'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default AddBookPage
