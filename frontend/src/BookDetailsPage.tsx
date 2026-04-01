import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'

interface CopyDetail {
  barcode: string
  status: string
}

interface BookDetails {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  copies: CopyDetail[]
}

function BookDetailsPage() {
  const { isbn } = useParams<{ isbn: string }>()
  const [book, setBook] = useState<BookDetails | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchBook = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/catalog/books/${isbn}`)
        if (response.status === 404) {
          setNotFound(true)
          return
        }
        if (!response.ok) {
          const data = await response.json()
          throw new Error(data.error || 'Request failed')
        }
        const data: BookDetails = await response.json()
        setBook(data)
      } catch (err) {
        if (!notFound) {
          setError(err instanceof Error ? err.message : 'Request failed')
        }
      } finally {
        setLoading(false)
      }
    }
    fetchBook()
  }, [isbn])

  if (loading) {
    return (
      <p className="text-text italic">Consulting the archives...</p>
    )
  }

  if (notFound) {
    return (
      <div className="text-center py-12 px-4">
        <p className="text-text-heading text-lg font-semibold font-heading m-0 mb-2">
          This tome is not recorded in our archives
        </p>
        <p className="text-text text-base italic m-0 mb-6">
          ISBN {isbn} was not found in the catalog
        </p>
        <Link
          to="/catalog/browse"
          className="py-2 px-4 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
        >
          Return to Browse
        </Link>
      </div>
    )
  }

  if (error) {
    return (
      <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded text-sm">
        {error}
      </p>
    )
  }

  if (!book) return null

  return (
    <div>
      <div className="mb-6">
        <Link
          to="/catalog/browse"
          className="text-sm font-semibold font-heading text-accent hover:underline tracking-wide"
        >
          ← Back to Browse
        </Link>
      </div>

      <div className="py-5 px-6 border border-border rounded bg-bg mb-6">
        <h2 className="text-2xl font-bold font-heading text-text-heading tracking-wide m-0 mb-1">
          {book.title}
        </h2>
        <p className="text-text italic text-base m-0 mb-3">{book.authors.join(', ')}</p>
        <p className="flex gap-2 items-center m-0">
          <span className="text-xs py-0.5 px-2 rounded font-semibold bg-code-bg text-text font-mono">
            ISBN {book.isbn}
          </span>
          <span className="text-xs py-0.5 px-2 rounded font-semibold bg-code-bg text-text">
            {book.publicationYear}
          </span>
        </p>
      </div>

      <h3 className="font-heading text-lg font-semibold text-text-heading tracking-wide mb-3">
        Copies
      </h3>

      {book.copies.length === 0 ? (
        <p className="text-text italic text-base">No copies have been registered for this tome.</p>
      ) : (
        <div className="flex flex-col gap-2">
          {book.copies.map((copy) => (
            <div
              key={copy.barcode}
              className="flex justify-between items-center py-3 px-4 border border-border rounded"
            >
              <span className="font-mono text-sm text-text-heading">{copy.barcode}</span>
              <span
                className={`text-xs py-0.5 px-2 rounded font-semibold uppercase tracking-widest ${
                  copy.status === 'Available'
                    ? 'bg-success-bg text-success'
                    : 'bg-unavailable-bg text-error'
                }`}
              >
                {copy.status}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default BookDetailsPage
