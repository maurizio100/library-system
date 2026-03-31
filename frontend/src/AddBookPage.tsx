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
    <div>
      <button
        onClick={onBack}
        className="py-1.5 px-3 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
      >
        Return to the Archives
      </button>
      <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
        Inscribe a Tome by ISBN
      </h2>

      {submitState === 'success' ? (
        <div className="text-center py-8 px-4 border border-success-border rounded bg-success-bg text-success">
          <p className="text-lg font-semibold font-heading m-0 mb-6">{successMessage}</p>
          <div className="flex gap-3 justify-center">
            <button
              onClick={handleReset}
              className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover"
            >
              Inscribe Another
            </button>
            <button
              onClick={onBack}
              className="py-2 px-4 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
            >
              Return to the Archives
            </button>
          </div>
        </div>
      ) : (
        <>
          <div className="mb-6">
            <div className="mb-4">
              <label htmlFor="isbn-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
                ISBN
              </label>
              <div className="flex gap-2">
                <input
                  id="isbn-input"
                  type="text"
                  value={isbn}
                  onChange={(e) => setIsbn(e.target.value)}
                  placeholder="Enter the 13-digit ISBN rune"
                  className="flex-1 py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)]"
                  disabled={lookupState === 'loading'}
                />
                <button
                  onClick={handleLookup}
                  disabled={!isbnValid || lookupState === 'loading'}
                  className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover disabled:bg-accent-disabled disabled:cursor-not-allowed"
                >
                  {lookupState === 'loading' ? 'Consulting the scribes...' : 'Consult'}
                </button>
              </div>
              {isbnTouched && !isbnValid && (
                <p className="text-error text-sm mt-1.5">The runes are malformed — check the ISBN</p>
              )}
            </div>
          </div>

          {errorMessage && (
            <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
              {errorMessage}
            </p>
          )}

          {showFields && (
            <div className="p-6 border border-border rounded bg-bg">
              <div className="mb-4">
                <label htmlFor="title-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
                  Title of the Work
                </label>
                <input
                  id="title-input"
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="flex-1 w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="authors-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
                  Scribes & Authors
                </label>
                <input
                  id="authors-input"
                  type="text"
                  value={authors}
                  onChange={(e) => setAuthors(e.target.value)}
                  placeholder="Comma-separated"
                  className="flex-1 w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="year-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
                  Year of Publication
                </label>
                <input
                  id="year-input"
                  type="text"
                  value={publicationYear}
                  onChange={(e) => setPublicationYear(e.target.value)}
                  className="flex-1 w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
                />
                {!publicationYear && (
                  <p className="text-warning text-sm mt-1.5 italic">The year of this work's creation is lost to time</p>
                )}
              </div>
              <button
                onClick={handleConfirm}
                disabled={!canConfirm}
                className="mt-2 py-3 px-6 text-base font-semibold font-heading bg-success text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-success-hover disabled:bg-success-disabled disabled:cursor-not-allowed"
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
