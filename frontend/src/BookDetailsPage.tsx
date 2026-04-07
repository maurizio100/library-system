import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getBook, registerCopy, removeCopy, type BookDetails } from './api/catalog'

function BookDetailsPage() {
  const { isbn } = useParams<{ isbn: string }>()
  const [book, setBook] = useState<BookDetails | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [barcode, setBarcode] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [removingBarcode, setRemovingBarcode] = useState<string | null>(null)
  const [removeError, setRemoveError] = useState('')

  useEffect(() => {
    const fetchBook = async () => {
      try {
        const data = await getBook(isbn!)
        if (data === null) {
          setNotFound(true)
        } else {
          setBook(data)
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Request failed')
      } finally {
        setLoading(false)
      }
    }
    fetchBook()
  }, [isbn])

  const handleRemoveCopy = async (copyBarcode: string) => {
    setRemovingBarcode(copyBarcode)
    setRemoveError('')
    try {
      await removeCopy(copyBarcode)
      setBook((prev) =>
        prev ? { ...prev, copies: prev.copies.filter((c) => c.barcode !== copyBarcode) } : prev
      )
    } catch (err) {
      setRemoveError(err instanceof Error ? err.message : 'Removal failed')
    } finally {
      setRemovingBarcode(null)
    }
  }

  const handleRegisterCopy = async () => {
    setSubmitting(true)
    setErrorMessage('')
    setSuccessMessage('')
    try {
      const data = await registerCopy(isbn!, barcode)
      setBook((prev) =>
        prev ? { ...prev, copies: [...prev.copies, { barcode: data.barcode, status: 'Available' }] } : prev
      )
      setSuccessMessage(`Copy "${data.barcode}" registered`)
      setBarcode('')
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setSubmitting(false)
    }
  }

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

      <div className="py-5 px-6 border border-border rounded bg-bg mb-6 flex gap-5 items-start">
        {book.coverUrl ? (
          <img
            src={book.coverUrl}
            alt={`Cover of ${book.title}`}
            className="w-24 rounded shadow-sm shrink-0 object-cover"
          />
        ) : (
          <div className="w-24 h-36 rounded bg-code-bg border border-border flex items-center justify-center shrink-0">
            <span className="text-xs text-text opacity-40 text-center leading-tight px-1">No cover</span>
          </div>
        )}
        <div className="flex-1">
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
      </div>

      <h3 className="font-heading text-lg font-semibold text-text-heading tracking-wide mb-3">
        Copies
      </h3>

      {removeError && (
        <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-3 text-sm">
          {removeError}
        </p>
      )}

      {book.copies.length === 0 ? (
        <p className="text-text italic text-base mb-6">No copies have been registered for this tome.</p>
      ) : (
        <div className="flex flex-col gap-2 mb-6">
          {book.copies.map((copy) => (
            <div
              key={copy.barcode}
              className="flex justify-between items-center py-3 px-4 border border-border rounded"
            >
              <span className="font-mono text-sm text-text-heading">{copy.barcode}</span>
              <div className="flex items-center gap-3">
                <span
                  className={`text-xs py-0.5 px-2 rounded font-semibold uppercase tracking-widest ${
                    copy.status === 'Available'
                      ? 'bg-success-bg text-success'
                      : 'bg-unavailable-bg text-error'
                  }`}
                >
                  {copy.status}
                </span>
                <button
                  onClick={() => handleRemoveCopy(copy.barcode)}
                  disabled={copy.status === 'Borrowed' || removingBarcode !== null}
                  title={copy.status === 'Borrowed' ? 'Cannot remove a borrowed copy' : 'Remove this copy'}
                  className="text-xs py-1 px-3 font-semibold font-heading text-error border border-error-border rounded bg-transparent cursor-pointer transition-colors hover:bg-error-bg disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  {removingBarcode === copy.barcode ? 'Removing…' : 'Remove'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="pt-4 border-t border-border">
        <h3 className="font-heading text-lg font-semibold text-text-heading tracking-wide mb-3">
          Register a Copy
        </h3>
        {successMessage && (
          <p className="text-success py-3 px-4 bg-success-bg border border-success-border rounded mb-4 text-sm">
            {successMessage}
          </p>
        )}
        {errorMessage && (
          <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
            {errorMessage}
          </p>
        )}
        <div className="flex gap-2 items-end">
          <div className="flex-1">
            <label
              htmlFor="barcode"
              className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide"
            >
              Barcode
            </label>
            <input
              id="barcode"
              type="text"
              value={barcode}
              onChange={(e) => {
                setBarcode(e.target.value)
                setErrorMessage('')
              }}
              placeholder="Enter copy barcode..."
              className="w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
              disabled={submitting}
            />
          </div>
          <button
            onClick={handleRegisterCopy}
            disabled={!barcode.trim() || submitting}
            className="py-3 px-6 text-base font-semibold font-heading bg-success text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-success-hover disabled:bg-success-disabled disabled:cursor-not-allowed"
          >
            {submitting ? 'Registering...' : 'Confirm'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default BookDetailsPage
