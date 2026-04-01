import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

type SubmitState = 'idle' | 'submitting' | 'success' | 'error'

interface Member {
  memberId: string
  name: string
}

interface LoanDetails {
  loanId: string
  memberName: string
  copyBarcode: string
  loanDate: string
  dueDate: string
}

function BorrowBookPage() {
  const navigate = useNavigate()
  const [members, setMembers] = useState<Member[]>([])
  const [memberSearch, setMemberSearch] = useState('')
  const [selectedMember, setSelectedMember] = useState<Member | null>(null)
  const [copyBarcode, setCopyBarcode] = useState('')
  const [submitState, setSubmitState] = useState<SubmitState>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [loanDetails, setLoanDetails] = useState<LoanDetails | null>(null)

  useEffect(() => {
    fetch('http://localhost:8080/api/lending/members')
      .then((res) => res.json())
      .then((data: Member[]) => setMembers(data))
      .catch(() => {})
  }, [])

  const filteredMembers: Member[] = memberSearch.trim()
    ? members.filter((m) =>
        m.name.toLowerCase().includes(memberSearch.trim().toLowerCase())
      )
    : members

  const canConfirm =
    submitState !== 'submitting' &&
    selectedMember !== null &&
    copyBarcode.trim().length > 0

  const handleSelectMember = (member: Member) => {
    setSelectedMember(member)
    setMemberSearch(member.name)
  }

  const handleMemberSearchChange = (value: string) => {
    setMemberSearch(value)
    if (selectedMember && value !== selectedMember.name) {
      setSelectedMember(null)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canConfirm || !selectedMember) return

    setSubmitState('submitting')
    setErrorMessage('')

    try {
      const response = await fetch('http://localhost:8080/api/lending/loans', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          memberId: selectedMember.memberId,
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
        memberName: selectedMember.name,
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
    setSelectedMember(null)
    setMemberSearch('')
    setCopyBarcode('')
    setSubmitState('idle')
    setErrorMessage('')
    setLoanDetails(null)
  }

  const showMemberList =
    !selectedMember && (memberSearch.trim().length > 0 || members.length > 0)

  return (
    <div>
      <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
        Borrow a Book
      </h2>

      {submitState === 'success' && loanDetails ? (
        <div className="text-center py-8 px-4 border border-success-border rounded bg-success-bg text-success">
          <p className="text-lg font-semibold font-heading m-0 mb-4">The tome has been entrusted to the borrower</p>
          <div className="mb-4">
            <p>Member: {loanDetails.memberName}</p>
            <p>Copy Barcode: {loanDetails.copyBarcode}</p>
            <p>Due Date: {loanDetails.dueDate}</p>
          </div>
          <div className="flex gap-3 justify-center">
            <button
              onClick={handleReset}
              className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover"
            >
              Lend Another
            </button>
            <button
              onClick={() => navigate('/catalog/browse')}
              className="py-2 px-4 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
            >
              Return to the Archives
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="p-6 border border-border rounded bg-bg">
          <div className="mb-4">
            <label htmlFor="member-search-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
              Member
            </label>
            <input
              id="member-search-input"
              type="text"
              value={memberSearch}
              onChange={(e) => handleMemberSearchChange(e.target.value)}
              placeholder="Search members by name"
              aria-label="Member Search"
              className="w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
              disabled={submitState === 'submitting'}
            />

            {showMemberList && (
              <ul
                role="listbox"
                aria-label="Member list"
                className="mt-1 border border-border rounded bg-bg max-h-48 overflow-y-auto"
              >
                {filteredMembers.length === 0 ? (
                  <li className="py-2 px-4 text-sm text-text italic">No members found</li>
                ) : (
                  filteredMembers.map((member) => (
                    <li
                      key={member.memberId}
                      role="option"
                      onClick={() => handleSelectMember(member)}
                      className="py-2 px-4 text-sm font-sans text-text-heading cursor-pointer hover:bg-accent-bg"
                    >
                      {member.name}
                    </li>
                  ))
                )}
              </ul>
            )}
          </div>

          <div className="mb-4">
            <label htmlFor="copy-barcode-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
              Copy Barcode
            </label>
            <input
              id="copy-barcode-input"
              type="text"
              value={copyBarcode}
              onChange={(e) => setCopyBarcode(e.target.value)}
              placeholder="The barcode inscribed upon the tome"
              className="w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
              disabled={submitState === 'submitting'}
            />
          </div>

          {selectedMember && copyBarcode.trim().length > 0 && submitState === 'idle' && (
            <div className="mb-4 text-text text-sm">
              <p>Member: {selectedMember.name}</p>
              <p>Copy Barcode: {copyBarcode.trim()}</p>
            </div>
          )}

          {errorMessage && (
            <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
              {errorMessage}
            </p>
          )}

          <button
            type="submit"
            disabled={!canConfirm}
            className="mt-2 py-3 px-6 text-base font-semibold font-heading bg-success text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-success-hover disabled:bg-success-disabled disabled:cursor-not-allowed"
          >
            {submitState === 'submitting' ? 'The scribes are writing...' : 'Confirm Loan'}
          </button>
        </form>
      )}
    </div>
  )
}

export default BorrowBookPage
