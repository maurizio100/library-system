import { useState, useEffect } from 'react'
import AddBookPage from './AddBookPage'
import './App.css'

interface BookSearchResult {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  totalCopies: number
  availableCopies: number
}

function App() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<BookSearchResult[]>([])
  const [searched, setSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [mode, setMode] = useState<'browse' | 'search'>('browse')
  const [page, setPage] = useState<'browse' | 'add-book'>('browse')

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

  if (page === 'add-book') {
    return (
      <div className="app">
        <header>
          <h1>The Great Library of Minas Tirith</h1>
          <p className="subtitle">A chronicle of all volumes known to the Realm of Gondor</p>
        </header>
        <AddBookPage onBack={() => { setPage('browse'); fetchBooks() }} />
      </div>
    )
  }

  return (
    <div className="app">
      <header>
        <h1>The Great Library of Minas Tirith</h1>
        <p className="subtitle">A chronicle of all volumes known to the Realm of Gondor</p>
      </header>

      <div className="toolbar">
        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search the archives by title, scribe, or ISBN..."
            className="search-input"
          />
          <button type="submit" disabled={loading} className="search-button">
            {loading ? 'Consulting the archives...' : 'Seek'}
          </button>
        </form>
        <div className="toolbar-actions">
          {mode === 'search' && (
            <button onClick={handleBrowseAll} className="browse-button" disabled={loading}>
              Reveal All Tomes
            </button>
          )}
          <button onClick={() => setPage('add-book')} className="add-book-button">
            Inscribe New Tome
          </button>
        </div>
      </div>

      {error && <p className="error">{error}</p>}

      {searched && results.length === 0 && !error && (
        <div className="empty-state">
          <p className="empty-title">
            {mode === 'search' ? `No scrolls match "${query}" in our records` : 'The shelves stand empty, awaiting their first tome'}
          </p>
          <p className="empty-hint">
            {mode === 'search' ? 'Perhaps the scribes recorded it under a different name' : 'Inscribe new volumes to fill the Great Library'}
          </p>
        </div>
      )}

      {results.length > 0 && (
        <div className="results">
          <div className="results-header">
            <p className="results-count">
              {mode === 'search'
                ? `${results.length} volume${results.length !== 1 ? 's' : ''} unearthed`
                : `${results.length} volume${results.length !== 1 ? 's' : ''} in the archive`}
            </p>
          </div>
          <div className="book-list">
            {results.map((book) => (
              <div key={book.isbn} className="book-card">
                <div className="book-info">
                  <h2>{book.title}</h2>
                  <p className="book-authors">{book.authors.join(', ')}</p>
                  <p className="book-meta">
                    <span className="isbn-badge">ISBN {book.isbn}</span>
                    <span className="year-badge">{book.publicationYear}</span>
                  </p>
                </div>
                <div
                  className={`availability ${book.availableCopies > 0 ? 'available' : 'unavailable'}`}
                >
                  <span className="copy-count">
                    {book.availableCopies}/{book.totalCopies}
                  </span>
                  <span className="copy-label">available</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default App
