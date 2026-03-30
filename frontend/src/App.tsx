import { useState } from 'react'
import './App.css'

interface BookSearchResult {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  availableCopies: number
}

function App() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<BookSearchResult[]>([])
  const [searched, setSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim()) return

    setLoading(true)
    setError('')
    try {
      const response = await fetch(
        `http://localhost:8080/api/catalog/books?q=${encodeURIComponent(query.trim())}`
      )
      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.error || 'Search failed')
      }
      const data: BookSearchResult[] = await response.json()
      setResults(data)
      setSearched(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Search failed')
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header>
        <h1>Library Catalog</h1>
      </header>

      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by title, author, or ISBN..."
          className="search-input"
        />
        <button type="submit" disabled={loading} className="search-button">
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <p className="error">{error}</p>}

      {searched && results.length === 0 && !error && (
        <p className="no-results">No books found for "{query}"</p>
      )}

      {results.length > 0 && (
        <div className="results">
          <p className="results-count">
            {results.length} book{results.length !== 1 ? 's' : ''} found
          </p>
          <div className="book-list">
            {results.map((book) => (
              <div key={book.isbn} className="book-card">
                <div className="book-info">
                  <h2>{book.title}</h2>
                  <p className="book-authors">{book.authors.join(', ')}</p>
                  <p className="book-meta">
                    ISBN: {book.isbn} &middot; {book.publicationYear}
                  </p>
                </div>
                <div
                  className={`availability ${book.availableCopies > 0 ? 'available' : 'unavailable'}`}
                >
                  <span className="copy-count">{book.availableCopies}</span>
                  <span className="copy-label">
                    {book.availableCopies === 1 ? 'copy' : 'copies'} available
                  </span>
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
