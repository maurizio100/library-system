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
        setErrorMessage('A book with this ISBN already exists in the catalog')
        return
      }

      const result = await lookupIsbn(isbn)
      if (!result) {
        setLookupState('not-found')
        setErrorMessage('No book found for this ISBN')
        return
      }

      setTitle(result.title)
      setAuthors(result.authors.join(', '))
      setPublicationYear(result.publicationYear != null ? String(result.publicationYear) : '')
      setLookupState('resolved')
    } catch {
      setLookupState('error')
      setErrorMessage('ISBN lookup service is currently unavailable — please try again later')
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
      setSuccessMessage('Book added to catalog')
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
        Back to Catalog
      </button>
      <h2>Add a Book by ISBN</h2>

      {submitState === 'success' ? (
        <div className="success-message">
          <p>{successMessage}</p>
          <div className="success-actions">
            <button onClick={handleReset} className="search-button">
              Add Another
            </button>
            <button onClick={onBack} className="browse-button">
              Back to Catalog
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
                  placeholder="Enter 13-digit ISBN"
                  className="search-input"
                  disabled={lookupState === 'loading'}
                />
                <button
                  onClick={handleLookup}
                  disabled={!isbnValid || lookupState === 'loading'}
                  className="search-button"
                >
                  {lookupState === 'loading' ? 'Looking up...' : 'Look Up'}
                </button>
              </div>
              {isbnTouched && !isbnValid && (
                <p className="validation-message">Invalid ISBN format</p>
              )}
            </div>
          </div>

          {errorMessage && <p className="error">{errorMessage}</p>}

          {showFields && (
            <div className="book-form">
              <div className="form-field">
                <label htmlFor="title-input">Title</label>
                <input
                  id="title-input"
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="search-input"
                />
              </div>
              <div className="form-field">
                <label htmlFor="authors-input">Authors</label>
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
                <label htmlFor="year-input">Publication Year</label>
                <input
                  id="year-input"
                  type="text"
                  value={publicationYear}
                  onChange={(e) => setPublicationYear(e.target.value)}
                  className="search-input"
                />
                {!publicationYear && (
                  <p className="warning-message">Publication year is missing</p>
                )}
              </div>
              <button
                onClick={handleConfirm}
                disabled={!canConfirm}
                className="confirm-button"
              >
                {submitState === 'submitting' ? 'Adding...' : 'Add to Catalog'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default AddBookPage
