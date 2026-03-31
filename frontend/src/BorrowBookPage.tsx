import { useState } from 'react'

type SubmitState = 'idle' | 'submitting' | 'success' | 'error'

interface LoanDetails {
  loanId: string
  memberId: string
  copyBarcode: string
  loanDate: string
  dueDate: string
}

interface BorrowBookPageProps {
  onBack: () => void
}

function BorrowBookPage({ onBack }: BorrowBookPageProps) {
  const [memberId, setMemberId] = useState('')
  const [copyBarcode, setCopyBarcode] = useState('')
  const [submitState, setSubmitState] = useState<SubmitState>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [loanDetails, setLoanDetails] = useState<LoanDetails | null>(null)

  const canConfirm =
    submitState !== 'submitting' &&
    memberId.trim().length > 0 &&
    copyBarcode.trim().length > 0

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canConfirm) return

    setSubmitState('submitting')
    setErrorMessage('')

    try {
      const response = await fetch('http://localhost:8080/api/lending/loans', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          memberId: memberId.trim(),
          copyBarcode: copyBarcode.trim(),
        }),
      })

      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.error || 'Failed to create loan')
      }

      const data = await response.json()
      setLoanDetails({
        loanId: data.loanId,
        memberId: data.memberId,
        copyBarcode: data.copyBarcode,
        loanDate: data.loanDate,
        dueDate: data.dueDate,
      })
      setSubmitState('success')
    } catch (err) {
      setSubmitState('error')
      setErrorMessage(err instanceof Error ? err.message : 'Failed to create loan')
    }
  }

  const handleReset = () => {
    setMemberId('')
    setCopyBarcode('')
    setSubmitState('idle')
    setErrorMessage('')
    setLoanDetails(null)
  }

  return (
    <div className="add-book-page">
      <button onClick={onBack} className="back-button">
        Return to the Archives
      </button>
      <h2>Borrow a Book</h2>

      {submitState === 'success' && loanDetails ? (
        <div className="success-message">
          <p>The tome has been entrusted to the borrower</p>
          <div className="loan-details">
            <p>Member ID: {loanDetails.memberId}</p>
            <p>Copy Barcode: {loanDetails.copyBarcode}</p>
            <p>Due Date: {loanDetails.dueDate}</p>
          </div>
          <div className="success-actions">
            <button onClick={handleReset} className="search-button">
              Lend Another
            </button>
            <button onClick={onBack} className="browse-button">
              Return to the Archives
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="book-form">
          <div className="form-field">
            <label htmlFor="member-id-input">Member ID</label>
            <input
              id="member-id-input"
              type="text"
              value={memberId}
              onChange={(e) => setMemberId(e.target.value)}
              placeholder="The borrower's membership identifier"
              className="search-input"
              disabled={submitState === 'submitting'}
            />
          </div>
          <div className="form-field">
            <label htmlFor="copy-barcode-input">Copy Barcode</label>
            <input
              id="copy-barcode-input"
              type="text"
              value={copyBarcode}
              onChange={(e) => setCopyBarcode(e.target.value)}
              placeholder="The barcode inscribed upon the tome"
              className="search-input"
              disabled={submitState === 'submitting'}
            />
          </div>

          {memberId.trim().length > 0 && copyBarcode.trim().length > 0 && submitState === 'idle' && (
            <div className="loan-summary">
              <p>Member ID: {memberId.trim()}</p>
              <p>Copy Barcode: {copyBarcode.trim()}</p>
            </div>
          )}

          {errorMessage && <p className="error">{errorMessage}</p>}

          <button
            type="submit"
            disabled={!canConfirm}
            className="confirm-button"
          >
            {submitState === 'submitting' ? 'The scribes are writing...' : 'Confirm Loan'}
          </button>
        </form>
      )}
    </div>
  )
}

export default BorrowBookPage
