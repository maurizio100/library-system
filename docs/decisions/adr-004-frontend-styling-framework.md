# ADR-004: Frontend Styling Framework

## Status
Accepted

## Context
The library system uses React 19 with TypeScript and Vite (see ADR-003) but has no styling solution yet. With UI stories (004, 005) approaching, we need a consistent styling approach. Key requirements: easy to use, modern, and backed by a large community for long-term support and developer onboarding.

## Options Considered

### Option A: Tailwind CSS
Utility-first CSS framework that provides low-level classes applied directly in JSX. Largest community in the React ecosystem (~13M weekly npm downloads). Excellent Vite integration with automatic purging of unused classes.
- **Pros:** Largest community, tiny production bundles, full creative freedom, no design opinion imposed, first-class Vite support
- **Cons:** No pre-built components (build from scratch or add a component library), JSX can get verbose, requires learning utility class names

### Option B: Material UI (MUI)
Pre-built component library implementing Google's Material Design. Mature ecosystem with strong TypeScript support.
- **Pros:** Large set of accessible components out of the box, consistent design with minimal effort, excellent documentation
- **Cons:** Opinionated Material Design look, heavier bundle size, customizing beyond Material Design is tedious

### Option C: shadcn/ui + Tailwind
Copy-paste component library built on Radix UI primitives and styled with Tailwind. Components are owned in your codebase, not an external dependency.
- **Pros:** Accessible pre-built components with full control, Tailwind underneath, fastest-growing option in the React community
- **Cons:** Smaller community, requires Tailwind knowledge, components are your code to maintain with no automatic upstream updates

### Option D: Bootstrap
The most mature CSS framework with a React wrapper (`react-bootstrap`). Familiar to most developers.
- **Pros:** Most mature, well-documented, large component library, easy to start
- **Cons:** Sites look generic without heavy customization, heavier bundles, declining mindshare in the React ecosystem

## Decision
We will use **Tailwind CSS** because it is the de facto standard in the modern React ecosystem, has the largest community, integrates seamlessly with our Vite setup, and gives us full creative freedom without imposing a design opinion. For a library system with moderate UI complexity, the utility-first approach keeps styling co-located with components while producing minimal production bundles. If pre-built components are needed later, shadcn/ui can be added on top without changing the styling approach.

## Consequences

### Positive
- Access to the largest styling community and ecosystem in React
- Small production bundles due to automatic purging of unused classes
- No design opinion imposed — UI can match any visual identity
- Co-located styles in JSX reduce context switching

### Negative
- No pre-built components — UI elements must be built from scratch or a component library added later
- JSX can become verbose with many utility classes
- Team needs to learn Tailwind's utility class naming conventions

### Neutral
- Can layer shadcn/ui on top later if pre-built components become necessary
- Design consistency relies on discipline (e.g., using Tailwind's theme config) rather than a built-in design system

## References
- [ADR-003 — Frontend Framework](adr-003-frontend-framework.md)
- [Tailwind CSS documentation](https://tailwindcss.com/docs)
