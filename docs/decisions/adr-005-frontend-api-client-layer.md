# ADR-005: Introduce a Frontend API Client Layer

## Status
Accepted

## Context
All API calls in the frontend are made via raw `fetch()` calls embedded directly inside page components. The base URL (`http://localhost:8080/api`) is hardcoded in each call site. Error handling, response parsing, and status checks are duplicated across every page.

This creates several problems:
- Changing the base URL or adding auth headers requires touching every page
- Error handling logic diverges over time, leading to inconsistent UX
- Pages are harder to test in isolation because network calls are interleaved with rendering logic
- There is no single place to add cross-cutting concerns (e.g., request logging, token injection, timeout defaults)

## Options Considered

### Option A: Keep raw fetch calls in page components
No change. Each page continues to own its own fetch logic.

**Pros:** No refactoring required, no new abstractions.  
**Cons:** Duplication grows with each new page, cross-cutting concerns cannot be applied consistently, testing requires mocking global fetch everywhere.

### Option B: Centralised API client module (`src/api/`)
Introduce a thin `src/api/` module with one file per bounded context (e.g., `catalog.ts`, `lending.ts`). Each file exports typed async functions that call a shared `apiFetch` base wrapper. Page components call these functions instead of `fetch` directly.

**Pros:** Single place for base URL, headers, and error handling. Typed return values improve safety. Easy to mock in tests. Natural seam for future auth token injection.  
**Cons:** Small upfront refactor of all existing pages.

### Option C: Third-party HTTP client (e.g., Axios)
Replace `fetch` with Axios for interceptors and a richer API.

**Pros:** Built-in interceptor support, automatic JSON parsing, better defaults.  
**Cons:** Adds a dependency for capabilities that a thin wrapper around `fetch` already provides. Overkill at this scale.

## Decision
We will introduce a **centralised API client module (Option B)**.

A shared `apiFetch` base function handles the base URL, `Content-Type` header, and unified error throwing. Bounded-context-specific files (`src/api/catalog.ts`, `src/api/lending.ts`) export typed functions for each endpoint. Page components call these functions and no longer contain raw `fetch` calls.

## Consequences

### Positive
- Base URL and default headers are configured in one place
- Consistent error handling across all API calls
- API functions are independently testable with MSW (already in the project)
- Clear seam for adding auth headers or request logging later

### Negative
- Requires refactoring all existing pages to use the new module
- One additional layer of indirection to navigate when debugging

### Neutral
- Each bounded context gets its own API file, mirroring the backend module structure

## References
- [ADR-003: Frontend Framework](adr-003-frontend-framework.md)
- [Catalog bounded context spec](../domain/bounded-contexts/catalog.md)
- [Lending bounded context spec](../domain/bounded-contexts/lending.md)
