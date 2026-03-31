import { useState } from 'react'

type SubmitState = 'idle' | 'submitting' | 'success' | 'error'

interface RegisterMemberPageProps {
  onBack: () => void
}

function RegisterMemberPage({ onBack }: RegisterMemberPageProps) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [submitState, setSubmitState] = useState<SubmitState>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [registeredId, setRegisteredId] = useState('')

  const nameTouched = name.length > 0
  const emailTouched = email.length > 0
  const emailValid = /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)
  const canSubmit =
    submitState !== 'submitting' &&
    name.trim().length > 0 &&
    emailValid

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canSubmit) return

    setSubmitState('submitting')
    setErrorMessage('')

    try {
      const response = await fetch('http://localhost:8080/api/lending/members', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: name.trim(),
          email: email.trim(),
        }),
      })

      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.error || 'Failed to register member')
      }

      const data = await response.json()
      setRegisteredId(data.memberId)
      setSubmitState('success')
      setSuccessMessage('A new soul has been inscribed into the rolls of the Library')
    } catch (err) {
      setSubmitState('error')
      setErrorMessage(err instanceof Error ? err.message : 'Failed to register member')
    }
  }

  const handleReset = () => {
    setName('')
    setEmail('')
    setSubmitState('idle')
    setErrorMessage('')
    setSuccessMessage('')
    setRegisteredId('')
  }

  return (
    <div className="add-book-page">
      <button onClick={onBack} className="back-button">
        Return to the Archives
      </button>
      <h2>Register a New Member</h2>

      {submitState === 'success' ? (
        <div className="success-message">
          <p>{successMessage}</p>
          <p className="member-id-display">Member ID: {registeredId}</p>
          <div className="success-actions">
            <button onClick={handleReset} className="search-button">
              Register Another
            </button>
            <button onClick={onBack} className="browse-button">
              Return to the Archives
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="book-form">
          <div className="form-field">
            <label htmlFor="member-name-input">Name</label>
            <input
              id="member-name-input"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Full name of the new member"
              className="search-input"
              disabled={submitState === 'submitting'}
            />
            {nameTouched && name.trim().length === 0 && (
              <p className="validation-message">A name is required to join the fellowship</p>
            )}
          </div>
          <div className="form-field">
            <label htmlFor="member-email-input">Email</label>
            <input
              id="member-email-input"
              type="text"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="A raven's address for correspondence"
              className="search-input"
              disabled={submitState === 'submitting'}
            />
            {emailTouched && !emailValid && (
              <p className="validation-message">The raven cannot find this address</p>
            )}
          </div>

          {errorMessage && <p className="error">{errorMessage}</p>}

          <button
            type="submit"
            disabled={!canSubmit}
            className="confirm-button"
          >
            {submitState === 'submitting' ? 'The scribes are writing...' : 'Inscribe into the Rolls'}
          </button>
        </form>
      )}
    </div>
  )
}

export default RegisterMemberPage
