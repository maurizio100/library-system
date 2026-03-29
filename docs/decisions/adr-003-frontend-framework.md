# ADR-003: Frontend Framework

## Status
Proposed

## Context
The library system is a full-stack web application (see [ADR-002](adr-002-tech-stack.md)) with a Kotlin/Spring Boot backend. We need to choose a frontend framework that:
- Keeps frontend complexity manageable — the core value of this system is in the domain, not in cutting-edge UI
- Has a large ecosystem and community for long-term support, component libraries, and hiring
- Integrates well with a Spring Boot API backend

The frontend will be a single-page application communicating with the backend via REST (or GraphQL, to be decided separately).

## Options Considered

### Option A: React
The most widely adopted frontend library. Component-based architecture with a massive ecosystem of UI component libraries (MUI, Ant Design, shadcn/ui), state management solutions, and tooling. Typically paired with TypeScript for type safety.

**Pros:** Largest community and ecosystem; abundant component libraries and templates; easy to find developers; excellent tooling (Vite, Next.js); strong TypeScript support; extensive learning resources.
**Cons:** Unopinionated — requires choosing and assembling many libraries (routing, state, forms); can lead to inconsistent patterns across a codebase without discipline; JSX is a love-it-or-hate-it paradigm.

### Option B: Angular
A full-featured, opinionated framework by Google. Comes with built-in routing, forms, HTTP client, and dependency injection. Uses TypeScript by default. Follows a module-based architecture with clear conventions.

**Pros:** Batteries-included — routing, forms, HTTP, DI all built in; strong conventions reduce decision fatigue; excellent TypeScript integration; good for large teams with strict patterns.
**Cons:** Steeper learning curve (modules, decorators, RxJS); heavier bundle size; more boilerplate than React; smaller ecosystem of third-party UI components; community has shrunk relative to React.

## Decision
We will use **React** (with TypeScript) because it best satisfies our driving forces:
- **Simplicity:** React's component model is straightforward to learn and reason about. While it's unopinionated, for a library system with moderate UI complexity, a lightweight setup (React + React Router + a simple state approach) keeps things simple without the overhead of Angular's full framework.
- **Ecosystem & community:** React has the largest frontend ecosystem by a wide margin — more component libraries, more tutorials, more community support, and more developers available. This reduces risk and accelerates development.

We will use **Vite** as the build tool for fast development feedback and **TypeScript** for type safety across the frontend codebase.

## Consequences

### Positive
- Access to the largest selection of UI component libraries and pre-built solutions
- Straightforward component model keeps the frontend approachable
- TypeScript provides type safety and better IDE support
- Vite offers fast HMR and build times
- Easy to find resources, examples, and developers

### Negative
- Need to make additional choices for routing, state management, and form handling (not built in)
- Without discipline, React projects can drift into inconsistent patterns
- No built-in dependency injection (unlike Angular), though this is less critical for frontend code

### Neutral
- The frontend is fully decoupled from the Spring Boot backend — they communicate via API only
- CSS/styling approach (Tailwind, CSS Modules, styled-components) is a separate decision

## References
- [ADR-002: Tech Stack](adr-002-tech-stack.md)
- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
