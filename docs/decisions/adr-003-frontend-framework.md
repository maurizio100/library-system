# ADR-003: Frontend Framework

## Status
Accepted

## Context
The library system has a Kotlin/Spring Boot backend (see ADR-002). We need a frontend framework that keeps complexity manageable — the core value is in the domain, not in cutting-edge UI.

## Options Considered
- **React** — Largest ecosystem, simple component model, unopinionated (requires assembling libraries).
- **Angular** — Batteries-included but steeper learning curve, heavier bundle, smaller ecosystem.

## Decision
**React** with TypeScript and Vite — largest ecosystem, straightforward component model, fast dev feedback via Vite. For a library system with moderate UI complexity, React's lightweight setup is preferable to Angular's full framework overhead.

## Consequences
- Access to the largest selection of UI component libraries
- TypeScript provides type safety; Vite provides fast HMR
- Need to make additional choices for routing and state management (not built in)
- Frontend is fully decoupled from backend — communication via REST API only
