import { useState, useEffect } from 'react'

interface BookSearchResult {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  totalCopies: number
  availableCopies: number
}

interface RegisterCopyState {
  isbn: string | null
  barcode: string
  submitting: boolean
  successMessage: string
  errorMessage: string
}

function BrowseBooksPage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<BookSearchResult[]>([])
  const [searched, setSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [mode, setMode] = useState<'browse' | 'search'>('browse')
  const [registerCopy, setRegisterCopy] = useState<RegisterCopyState>({
    isbn: null,
    barcode: '',
    submitting: false,
    successMessage: '',
    errorMessage: '',
  })

  const fetchBooks = async (searchQuery?: string) => {
    setLoading(true)
    setError('')
    try {
      const url = searchQuery
        ? `http://localhost:8080/api/catalog/books?q=${encodeURIComponent(searchQuery)}`
        : 'http://localhost:8080/api/catalog/books'
      const response = await fetch(url)
      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.error || 'Request failed')
      }
      const data: BookSearchResult[] = await response.json()
      setResults(data)
      setSearched(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed')
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBooks()
  }, [])

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim()) return
    setMode('search')
    fetchBooks(query.trim())
  }

  const handleBrowseAll = () => {
    setQuery('')
    setMode('browse')
    fetchBooks()
  }

  const openRegisterForm = (isbn: string) => {
    setRegisterCopy({
      isbn,
      barcode: '',
      submitting: false,
      successMessage: '',
      errorMessage: '',
    })
  }

  const closeRegisterForm = () => {
    setRegisterCopy({
      isbn: null,
      barcode: '',
      submitting: false,
      successMessage: '',
      errorMessage: '',
    })
  }

  const handleRegisterCopy = async (isbn: string) => {
    setRegisterCopy((prev) => ({ ...prev, submitting: true, errorMessage: '', successMessage: '' }))
    try {
      const response = await fetch(
        `http://localhost:8080/api/catalog/books/${isbn}/copies`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ barcode: registerCopy.barcode }),
        }
      )
      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.error || 'Registration failed')
      }
      const data = await response.json()
      setResults((prev) =>
        prev.map((book) =>
          book.isbn === isbn
            ? { ...book, totalCopies: book.totalCopies + 1, availableCopies: book.availableCopies + 1 }
            : book
        )
      )
      setRegisterCopy((prev) => ({
        ...prev,
        barcode: '',
        submitting: false,
        successMessage: `Copy "${data.barcode}" for "${results.find((b) => b.isbn === isbn)?.title}" registered`,
        errorMessage: '',
      }))
    } catch (err) {
      setRegisterCopy((prev) => ({
        ...prev,
        submitting: false,
        errorMessage: err instanceof Error ? err.message : 'Registration failed',
        successMessage: '',
      }))
    }
  }

  return (
    <div>
      <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
        Browse Books
      </h2>

      <div className="flex flex-col gap-3 mb-8">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search the archives by title, scribe, or ISBN..."
            className="flex-1 py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)]"
          />
          <button
            type="submit"
            disabled={loading}
            className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover disabled:bg-accent-disabled disabled:cursor-not-allowed"
          >
            {loading ? 'Consulting the archives...' : 'Seek'}
          </button>
        </form>
        {mode === 'search' && (
          <div>
            <button
              onClick={handleBrowseAll}
              className="self-start py-2 px-4 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={loading}
            >
              Reveal All Tomes
            </button>
          </div>
        )}
      </div>

      {error && (
        <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
          {error}
        </p>
      )}

      {searched && results.length === 0 && !error && (
        <div className="text-center py-12 px-4">
          <p className="text-text-heading text-lg font-semibold font-heading m-0 mb-2">
            {mode === 'search' ? `No scrolls match "${query}" in our records` : 'The shelves stand empty, awaiting their first tome'}
          </p>
          <p className="text-text text-base italic m-0">
            {mode === 'search' ? 'Perhaps the scribes recorded it under a different name' : 'Inscribe new volumes to fill the Great Library'}
          </p>
        </div>
      )}

      {results.length > 0 && (
        <div>
          <div className="mb-4">
            <p className="text-text text-sm font-semibold italic m-0">
              {mode === 'search'
                ? `${results.length} volume${results.length !== 1 ? 's' : ''} unearthed`
                : `${results.length} volume${results.length !== 1 ? 's' : ''} in the archive`}
            </p>
          </div>
          <div className="flex flex-col gap-3">
            {results.map((book) => (
              <div
                key={book.isbn}
                className="py-5 px-6 border border-border rounded bg-bg transition-all hover:shadow-[0_4px_12px_rgba(44,24,16,0.08)] hover:border-accent"
              >
                <div className="flex justify-between items-center">
                  <div className="flex-1 min-w-0">
                    <h2 className="text-lg font-bold font-heading text-text-heading tracking-wide m-0 mb-1">
                      {book.title}
                    </h2>
                    <p className="text-text italic text-base m-0 mb-2">{book.authors.join(', ')}</p>
                    <p className="flex gap-2 items-center m-0">
                      <span className="text-xs py-0.5 px-2 rounded font-semibold bg-code-bg text-text font-mono">
                        ISBN {book.isbn}
                      </span>
                      <span className="text-xs py-0.5 px-2 rounded font-semibold bg-code-bg text-text">
                        {book.publicationYear}
                      </span>
                    </p>
                  </div>
                  <div className="flex items-center gap-3 shrink-0 ml-4">
                    <div
                      className={`flex flex-col items-center justify-center min-w-[72px] py-2.5 px-3 rounded ${
                        book.availableCopies > 0
                          ? 'bg-success-bg text-success'
                          : 'bg-unavailable-bg text-error'
                      }`}
                    >
                      <span className="text-xl font-bold leading-tight font-heading">
                        {book.availableCopies}/{book.totalCopies}
                      </span>
                      <span className="text-[0.7rem] font-semibold uppercase tracking-widest">
                        available
                      </span>
                    </div>
                    {registerCopy.isbn !== book.isbn && (
                      <button
                        onClick={() => openRegisterForm(book.isbn)}
                        className="py-2 px-4 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
                      >
                        Register Copy
                      </button>
                    )}
                  </div>
                </div>
                {registerCopy.isbn === book.isbn && (
                  <div className="mt-4 pt-4 border-t border-border">
                    {registerCopy.successMessage && (
                      <p className="text-success py-3 px-4 bg-success-bg border border-success-border rounded mb-4 text-sm">
                        {registerCopy.successMessage}
                      </p>
                    )}
                    {registerCopy.errorMessage && (
                      <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
                        {registerCopy.errorMessage}
                      </p>
                    )}
                    <div className="flex gap-2 items-end">
                      <div className="flex-1">
                        <label
                          htmlFor={`barcode-${book.isbn}`}
                          className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide"
                        >
                          Barcode
                        </label>
                        <input
                          id={`barcode-${book.isbn}`}
                          type="text"
                          value={registerCopy.barcode}
                          onChange={(e) =>
                            setRegisterCopy((prev) => ({ ...prev, barcode: e.target.value, errorMessage: '' }))
                          }
                          placeholder="Enter copy barcode..."
                          className="w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
                          disabled={registerCopy.submitting}
                        />
                      </div>
                      <button
                        onClick={() => handleRegisterCopy(book.isbn)}
                        disabled={!registerCopy.barcode.trim() || registerCopy.submitting}
                        className="py-3 px-6 text-base font-semibold font-heading bg-success text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-success-hover disabled:bg-success-disabled disabled:cursor-not-allowed"
                      >
                        {registerCopy.submitting ? 'Registering...' : 'Confirm'}
                      </button>
                      <button
                        onClick={closeRegisterForm}
                        className="py-3 px-6 text-base font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default BrowseBooksPage
