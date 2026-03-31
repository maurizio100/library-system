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
    <div>
      <button
        onClick={onBack}
        className="py-1.5 px-3 text-sm font-semibold font-heading bg-transparent text-accent border-[1.5px] border-accent rounded cursor-pointer transition-all tracking-wide hover:bg-accent-bg"
      >
        Return to the Archives
      </button>
      <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
        Register a New Member
      </h2>

      {submitState === 'success' ? (
        <div className="text-center py-8 px-4 border border-success-border rounded bg-success-bg text-success">
          <p className="text-lg font-semibold font-heading m-0 mb-4">{successMessage}</p>
          <p className="font-mono text-sm text-text mb-4">Member ID: {registeredId}</p>
          <div className="flex gap-3 justify-center">
            <button
              onClick={handleReset}
              className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover"
            >
              Register Another
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
        <form onSubmit={handleSubmit} className="p-6 border border-border rounded bg-bg">
          <div className="mb-4">
            <label htmlFor="member-name-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
              Name
            </label>
            <input
              id="member-name-input"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Full name of the new member"
              className="w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
              disabled={submitState === 'submitting'}
            />
            {nameTouched && name.trim().length === 0 && (
              <p className="text-error text-sm mt-1.5">A name is required to join the fellowship</p>
            )}
          </div>
          <div className="mb-4">
            <label htmlFor="member-email-input" className="block text-sm font-semibold font-heading text-text-heading mb-1.5 tracking-wide">
              Email
            </label>
            <input
              id="member-email-input"
              type="text"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="A raven's address for correspondence"
              className="w-full py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)] box-border"
              disabled={submitState === 'submitting'}
            />
            {emailTouched && !emailValid && (
              <p className="text-error text-sm mt-1.5">The raven cannot find this address</p>
            )}
          </div>

          {errorMessage && (
            <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded mb-4 text-sm">
              {errorMessage}
            </p>
          )}

          <button
            type="submit"
            disabled={!canSubmit}
            className="mt-2 py-3 px-6 text-base font-semibold font-heading bg-success text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-success-hover disabled:bg-success-disabled disabled:cursor-not-allowed"
          >
            {submitState === 'submitting' ? 'The scribes are writing...' : 'Inscribe into the Rolls'}
          </button>
        </form>
      )}
    </div>
  )
}

export default RegisterMemberPage
