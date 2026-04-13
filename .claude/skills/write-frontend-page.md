# Skill: write-frontend-page

Implement a frontend page following the project's React + TypeScript + Tailwind conventions (ADRs 003–006).

## Usage

```
/write-frontend-page "<page description>"
```

Example:
- `/write-frontend-page "suspend a member form"`
- `/write-frontend-page "overdue loans list"`

## Before you start

- Read the `.feature` file for the story — UI scenarios describe the exact form fields, error messages, button states, and success messages
- Read `docs/decisions/adr-005-frontend-api-client-layer.md` and `adr-006-frontend-custom-hooks.md`
- Check `frontend/src/api/` for existing functions before adding new ones

---

## Architecture rule

**Pages are thin JSX shells. Logic lives in hooks.**

```
src/
├── api/
│   ├── client.ts          ← apiFetch, apiFetchVoid, apiFetchNullable
│   ├── catalog.ts         ← catalog API functions + TypeScript interfaces
│   └── lending.ts         ← lending API functions + TypeScript interfaces
├── <Feature>Page.tsx      ← JSX only, imports hook and renders
└── use<Feature>.ts        ← all state, handlers, API calls
```

---

## Step 1 — Add API functions (if new endpoints are needed)

Add to `src/api/catalog.ts` or `src/api/lending.ts`. Never use raw `fetch` in components or hooks — always go through `apiFetch`.

```typescript
// src/api/lending.ts

export interface SuspendMemberResponse { memberId: string; status: string }

export function suspendMember(memberId: string): Promise<SuspendMemberResponse> {
  return apiFetch(`/lending/members/${encodeURIComponent(memberId)}/suspend`, { method: 'POST' })
}
```

Use the correct base function:
| Function | When to use |
|---|---|
| `apiFetch<T>` | Expects a JSON response body |
| `apiFetchVoid` | No response body (e.g. DELETE, some POSTs) |
| `apiFetchNullable<T>` | Response may be 404 (returns `null`) |

---

## Step 2 — Write the hook

One hook per page. File: `src/use<Feature>.ts`.

The hook owns all state, all handlers, and all API calls. It returns only what the page needs to render.

**State pattern — use discriminated union strings for async states:**

```typescript
type SubmitState = 'idle' | 'submitting' | 'success' | 'error'
```

**Template:**

```typescript
import { useState } from 'react'
import { suspendMember } from './api/lending'

export function useSuspendMember() {
  const [memberId, setMemberId]       = useState('')
  const [submitState, setSubmitState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const canSubmit = memberId.trim().length > 0 && submitState !== 'submitting'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canSubmit) return
    setSubmitState('submitting')
    setErrorMessage('')
    try {
      await suspendMember(memberId.trim())
      setSuccessMessage(`Member ${memberId} has been suspended.`)
      setSubmitState('success')
      setMemberId('')
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Request failed')
      setSubmitState('error')
    }
  }

  return { memberId, setMemberId, canSubmit, submitState, errorMessage, successMessage, handleSubmit }
}
```

---

## Step 3 — Write the page component

The page imports the hook and renders. No `useState`, no `fetch`, no business logic here.

```typescript
import { useSuspendMember } from './useSuspendMember'

function SuspendMemberPage() {
  const { memberId, setMemberId, canSubmit, submitState, errorMessage, successMessage, handleSubmit } = useSuspendMember()

  return (
    <div>
      <h2 className="font-heading text-2xl text-text-heading my-4 mb-6 tracking-wide">
        Suspend Member
      </h2>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4 max-w-md">
        <input
          type="text"
          value={memberId}
          onChange={(e) => setMemberId(e.target.value)}
          placeholder="Member ID"
          className="py-3 px-4 text-base font-sans border-2 border-border rounded outline-none bg-bg text-text-heading transition-colors focus:border-accent focus:shadow-[0_0_0_2px_var(--color-accent-bg)]"
        />

        {errorMessage && (
          <p className="text-error py-3 px-4 bg-error-bg border border-error-border rounded text-sm">
            {errorMessage}
          </p>
        )}

        {successMessage && (
          <p className="text-success py-3 px-4 bg-success-bg border border-success rounded text-sm">
            {successMessage}
          </p>
        )}

        <button
          type="submit"
          disabled={!canSubmit}
          className="py-3 px-6 text-base font-semibold font-heading bg-accent text-bg border-none rounded cursor-pointer transition-colors tracking-wide hover:bg-accent-hover disabled:bg-accent-disabled disabled:cursor-not-allowed"
        >
          {submitState === 'submitting' ? 'Processing...' : 'Suspend Member'}
        </button>
      </form>
    </div>
  )
}

export default SuspendMemberPage
```

---

## Tailwind CSS conventions

Use CSS custom properties (not hardcoded colors). The project uses a theme system.

| Token | Usage |
|---|---|
| `text-text-heading` | Headings and labels |
| `text-text` | Body copy, secondary text |
| `text-error` / `bg-error-bg` / `border-error-border` | Error states |
| `text-success` / `bg-success-bg` | Success states |
| `bg-accent` / `hover:bg-accent-hover` / `disabled:bg-accent-disabled` | Primary buttons |
| `border-border` | Default borders |
| `focus:border-accent` | Focused inputs |
| `font-heading` | Headings and button labels |
| `font-sans` | Body / input text |
| `font-mono` / `bg-code-bg` | Code/ISBN badges |

---

## Routing

If the page needs a route, add it to the router in `src/App.tsx` (or wherever routes are defined). Follow the path patterns already in use:
- Catalog: `/catalog/<slug>`
- Lending: `/lending/<slug>`
- Members: `/members/<slug>`

---

## Checklist before committing

- [ ] No `useState` / `fetch` / API imports in the page component
- [ ] Hook returns only what the page renders — no internal implementation details
- [ ] Button disabled when form is invalid or request is in-flight
- [ ] Error messages match the exact wording from the `.feature` file
- [ ] Success message shown after a confirmed operation
- [ ] API functions added to `src/api/catalog.ts` or `src/api/lending.ts`, not inline
- [ ] No hardcoded colors — use Tailwind theme tokens only
