import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { isValidIsbn, lookupIsbn } from './isbnLookup'
import {
  checkIsbnExists,
  searchBooksByTitle,
  addBook,
  type TitleSearchCandidate,
} from './api/catalog'

type SearchMode = 'isbn' | 'title'
type LookupState = 'idle' | 'loading' | 'resolved' | 'not-found' | 'error' | 'already-exists'
type TitleSearchState = 'idle' | 'loading' | 'results' | 'no-results' | 'error'
type SubmitState = 'idle' | 'submitting' | 'success' | 'error'

function AddBookPage() {
  const navigate = useNavigate()

  // mode
  const [mode, setMode] = useState<SearchMode>('isbn')

  // ISBN mode state
  const [isbn, setIsbn] = useState('')
  const [lookupState, setLookupState] = useState<LookupState>('idle')

  // title search state
  const [titleQuery, setTitleQuery] = useState('')
  const [titleSearchState, setTitleSearchState] = useState<TitleSearchState>('idle')
  const [titleResults, setTitleResults] = useState<TitleSearchCandidate[]>([])
  const [titleQueryTouched, setTitleQueryTouched] = useState(false)

  // shared resolved book state
  const [resolvedIsbn, setResolvedIsbn] = useState('')
  const [title, setTitle] = useState('')
  const [authors, setAuthors] = useState('')
  const [publicationYear, setPublicationYear] = useState('')

  // submission state
  const [submitState, setSubmitState] = useState<SubmitState>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const isbnTouched = isbn.length > 0
  const isbnValid = isValidIsbn(isbn)
  const showConfirmForm = lookupState === 'resolved' || (titleSearchState === 'results' && resolvedIsbn !== '')
  const canConfirm =
    showConfirmForm &&
    submitState !== 'submitting' &&
    title.trim().length > 0 &&
    authors.trim().length > 0

  // ── ISBN lookup ──

  const handleIsbnLookup = async () => {
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

      setResolvedIsbn(isbn)
      setTitle(result.title)
      setAuthors(result.authors.join(', '))
      setPublicationYear(result.publicationYear != null ? String(result.publicationYear) : '')
      setLookupState('resolved')
    } catch {
      setLookupState('error')
      setErrorMessage('ISBN lookup service is currently unavailable — please try again later')
    }
  }

  // ── Title search ──

  const handleTitleSearch = async () => {
    setTitleQueryTouched(true)
    if (!titleQuery.trim()) {
      setErrorMessage('Please enter a title to search')
      return
    }

    setTitleSearchState('loading')
    setErrorMessage('')
    setResolvedIsbn('')
    setTitle('')
    setAuthors('')
    setPublicationYear('')

    try {
      const results = await searchBooksByTitle(titleQuery)
      setTitleResults(results)
      if (results.length === 0) {
        setTitleSearchState('no-results')
      } else if (results.length === 1) {
        // auto-select the single result
        selectCandidate(results[0])
        setTitleSearchState('results')
      } else {
        setTitleSearchState('results')
      }
    } catch {
      setTitleSearchState('error')
      setErrorMessage('Book search service is currently unavailable — please try again later')
    }
  }

  const selectCandidate = async (candidate: TitleSearchCandidate) => {
    const exists = await checkIsbnExists(candidate.isbn)
    if (exists) {
      setErrorMessage('A book with this ISBN already exists in the catalog')
      setResolvedIsbn('')
      return
    }
    setResolvedIsbn(candidate.isbn)
    setTitle(candidate.title)
    setAuthors(candidate.authors.join(', '))
    setPublicationYear(candidate.publicationYear != null ? String(candidate.publicationYear) : '')
    setErrorMessage('')
  }

  const handleBackToResults = () => {
    setResolvedIsbn('')
    setTitle('')
    setAuthors('')
    setPublicationYear('')
    setErrorMessage('')
  }

  // ── Confirm & submit ──

  const handleConfirm = async () => {
    if (!canConfirm) return

    setSubmitState('submitting')
    setErrorMessage('')

    try {
      await addBook({
        isbn: resolvedIsbn,
        title: title.trim(),
        authors: authors.split(',').map((a) => a.trim()).filter(Boolean),
        publicationYear: publicationYear ? parseInt(publicationYear, 10) : 0,
      })
      setSubmitState('success')
      setSuccessMessage('The tome has been inscribed into the Great Library')
    } catch (err) {
      setSubmitState('error')
      setErrorMessage(err instanceof Error ? err.message : 'Failed to add book')
    }
  }

  const handleReset = () => {
    setMode('isbn')
    setIsbn('')
    setLookupState('idle')
    setTitleQuery('')
    setTitleSearchState('idle')
    setTitleResults([])
    setTitleQueryTouched(false)
    setResolvedIsbn('')
    setTitle('')
    setAuthors('')
    setPublicationYear('')
    setSubmitState('idle')
    setErrorMessage('')
    setSuccessMessage('')
  }

  const switchMode = (newMode: SearchMode) => {
    setMode(newMode)
    setErrorMessage('')
    setLookupState('idle')
    setTitleSearchState('idle')
    setTitleResults([])
    setResolvedIsbn('')
    setTitle('')
    setAuthors('')
    setPublicationYear('')
  }

  // ── Render ──

  if (submitState === 'success') {
    return (
      <div>
        <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
          Inscribe a Tome
        </h2>
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
              onClick={() => navigate('/catalog/browse')}
              className="py-2 px-4 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
            >
              Return to the Archives
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div>
      <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
        Inscribe a Tome
      </h2>

      {/* Mode tabs */}
      <div className="flex gap-0 mb-6 border-b border-border">
        <button
          onClick={() => switchMode('isbn')}
          className={`py-2 px-5 text-sm font-semibold font-heading border-b-2 transition-colors tracking-wide ${
            mode === 'isbn'
              ? 'border-accent text-accent'
              : 'border-transparent text-text-heading opacity-60 hover:opacity-100'
          }`}
        >
          Search by ISBN
        </button>
        <button
          onClick={() => switchMode('title')}
          className={`py-2 px-5 text-sm font-semibold font-heading border-b-2 transition-colors tracking-wide ${
            mode === 'title'
              ? 'border-accent text-accent'
              : 'border-transparent text-text-heading opacity-60 hover:opacity-100'
          }`}
        >
          Search by title
        </button>
      </div>

      {/* ISBN lookup */}
      {mode === 'isbn' && (
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
                placeholder="Enter the ISBN rune (e.g. 9780134685991 or 978-0-13-468599-1)"
                className="flex-1 py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)]"
                disabled={lookupState === 'loading'}
              />
              <button
                onClick={handleIsbnLookup}
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
      )}

      {/* Title search */}
      {mode === 'title' && (
        <div className="mb-6">
          <div className="mb-4">
            <label htmlFor="title-query-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
              Title
            </label>
            <div className="flex gap-2">
              <input
                id="title-query-input"
                type="text"
                value={titleQuery}
                onChange={(e) => setTitleQuery(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleTitleSearch() }}
                placeholder="Enter the title of the tome"
                className="flex-1 py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)]"
                disabled={titleSearchState === 'loading'}
              />
              <button
                onClick={handleTitleSearch}
                disabled={titleSearchState === 'loading'}
                className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover disabled:bg-accent-disabled disabled:cursor-not-allowed"
              >
                {titleSearchState === 'loading' ? 'Searching the archives...' : 'Search'}
              </button>
            </div>
            {titleQueryTouched && !titleQuery.trim() && (
              <p className="text-error text-sm mt-1.5">Please enter a title to search</p>
            )}
          </div>

          {/* Results list — shown when multiple results and no selection yet */}
          {titleSearchState === 'results' && titleResults.length > 1 && !resolvedIsbn && (
            <div className="border border-border rounded overflow-hidden">
              <p className="px-4 py-2 text-xs font-semibold font-heading text-text-heading uppercase tracking-wider bg-bg border-b border-border">
                Select a tome from the results
              </p>
              <ul className="divide-y divide-border m-0 p-0 list-none">
                {titleResults.map((candidate) => (
                  <li key={candidate.isbn}>
                    <button
                      onClick={() => selectCandidate(candidate)}
                      className="w-full text-left px-4 py-3 hover:bg-accent-bg transition-colors"
                    >
                      <span className="block font-semibold font-heading text-text-heading text-sm">
                        {candidate.title}
                      </span>
                      <span className="text-xs text-text-heading opacity-70">
                        {candidate.authors.join(', ')}
                        {candidate.publicationYear ? ` · ${candidate.publicationYear}` : ''}
                        {` · ISBN ${candidate.isbn}`}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {titleSearchState === 'no-results' && (
            <p className="text-text-heading opacity-70 italic text-sm py-2">
              No books found for this title
            </p>
          )}
        </div>
      )}

      {/* Error message */}
      {errorMessage && (
        <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
          {errorMessage}
        </p>
      )}

      {/* Confirm form — shared by both modes */}
      {showConfirmForm && (
        <div className="p-6 border border-border rounded bg-bg">
          {mode === 'title' && titleResults.length > 1 && (
            <button
              onClick={handleBackToResults}
              className="mb-4 text-xs font-semibold font-heading text-accent hover:underline"
            >
              ← Back to results
            </button>
          )}
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
    </div>
  )
}

export default AddBookPage
