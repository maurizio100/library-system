# ADR-006: Separate Page Logic into Custom Hooks

## Status
Proposed

## Context
Page components in the frontend currently mix three distinct responsibilities: data fetching, business/validation logic, and rendering. For example, `AddBookPage.tsx` manages ISBN lookup state, form submission, multi-step flow transitions, and conditional rendering all within a single component.

This creates several problems:
- Business logic and data fetching cannot be unit-tested without rendering the full component
- Components grow large and are difficult to reason about at a glance
- The same stateful patterns (loading, error, submission state machine) are re-implemented per page with no shared structure
- Refactoring the API layer (see ADR-005) becomes harder when fetch calls are scattered through render logic

## Options Considered

### Option A: Keep all logic inside page components
No change. State, effects, handlers, and JSX remain in one file per page.

**Pros:** No new files or abstractions, familiar pattern for simple forms.  
**Cons:** Logic is untestable in isolation, components grow unbounded, duplication of stateful patterns accelerates as features are added.

### Option B: Extract logic into custom hooks (`use<Feature>`)
Move non-rendering concerns out of page components into co-located custom hooks (e.g., `useAddBook.ts`, `useBookSearch.ts`). Each hook owns: state, derived values, event handlers, and API calls (via the API client from ADR-005). The page component becomes a thin shell that maps hook output to JSX.

**Pros:** Logic is independently testable with `renderHook`. Components are easier to read. State machine patterns can be standardised in hooks. Aligns with idiomatic React.  
**Cons:** More files per feature. Requires discipline to avoid hooks that are too tightly coupled to a single component's UI concerns.

### Option C: Lift state to a global store (e.g., Redux, Zustand)
Move shared state out of pages into a centralised store.

**Pros:** Cross-page state sharing becomes straightforward.  
**Cons:** Premature at this scale — no cross-page state sharing is needed yet. Adds significant framework overhead. Can be introduced later on top of this approach if required.

## Decision
We will **extract page logic into custom hooks (Option B)**.

Each page that contains non-trivial logic gets a co-located `use<PageName>.ts` hook. The hook owns all state, handlers, and API calls. The page component only renders. Hooks are placed alongside their page file (e.g., `AddBookPage.tsx` / `useAddBook.ts`). This decision is complementary to ADR-005 — hooks call the API client, not raw fetch.

## Consequences

### Positive
- Logic is testable with `renderHook` without mounting UI
- Page components are reduced to a readable mapping of state to JSX
- Common patterns (loading/error/success state machine) can be standardised once in hooks
- Clear separation of concerns makes onboarding easier

### Negative
- Each significant page now spans at least two files
- Risk of hooks leaking UI-specific concerns if discipline is not maintained (a hook should not know about CSS classes or DOM structure)

### Neutral
- Simple pages with trivial state (e.g., `NotFoundPage`) do not need a hook — this applies only where logic is non-trivial

## References
- [ADR-005: Frontend API Client Layer](adr-005-frontend-api-client-layer.md)
- [ADR-003: Frontend Framework](adr-003-frontend-framework.md)
