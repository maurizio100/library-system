# Library System

A modular monolith for managing a public library's catalog and lending operations. Built with Kotlin, Spring Boot 3.x, React, and Gradle.

## Directory Layout

```
├── CLAUDE.md              ← you are here
├── Backlog.md             ← active stories: Ready / In Progress (Done → docs/stories/done-archive.md)
├── .claude/skills/        ← project-level agent skills (see Skills section below)
├── docs/
│   ├── domain/
│   │   ├── context-map.md
│   │   ├── glossary.md
│   │   └── bounded-contexts/
│   │       ├── catalog.md
│   │       └── lending.md
│   ├── architecture/
│   │   ├── target-architecture.md
│   │   ├── solution-strategy.md
│   │   ├── quality-attributes.md
│   │   └── context.md
│   ├── decisions/          ← ADRs (see ADR Index below)
│   └── stories/
│       ├── catalog/        ← .feature files for Catalog epic
│       ├── lending/        ← .feature files for Lending epic
│       └── done-archive.md ← completed stories
├── shared/                 ← shared domain events module
│   └── src/main/kotlin/com/library/shared/events/
├── catalog/                ← catalog bounded context module
│   ├── src/main/kotlin/com/library/catalog/
│   │   ├── domain/         ← entities, value objects, events, repository interfaces
│   │   ├── api/            ← REST controllers, DTOs
│   │   └── infra/          ← JPA repositories, Spring event publishers
│   └── src/test/kotlin/com/library/catalog/
│       ├── domain/         ← unit tests
│       ├── bdd/            ← Cucumber step definitions
│       └── arch/           ← ArchUnit tests
├── application/            ← Spring Boot application entry point
│   └── src/main/kotlin/com/library/
└── build.gradle.kts        ← root build file
```

## Bounded Contexts

This system has two bounded contexts. **Never mix code between them.**

| Context | Gradle Module | REST Base Path | Responsibility | Spec |
|---|---|---|---|---|
| Catalog | `:catalog` | `/api/catalog` | Owns the library's collection of books and physical copies. Source of truth for what exists and whether copies are available. | [catalog.md](docs/domain/bounded-contexts/catalog.md) |
| Lending | `:lending` | `/api/lending` | Owns members, loans, returns, and overdue fees. Source of truth for who has borrowed what and what they owe. | [lending.md](docs/domain/bounded-contexts/lending.md) |

Cross-context communication happens **only** through domain events in the `:shared` module.

## Coding Conventions

### Domain Layer (`domain/`)
- **No Spring imports.** Domain classes must be pure Kotlin — no `@Component`, `@Service`, `@Autowired`, `@Entity`.
- Entities use the names from the [glossary](docs/domain/glossary.md) exactly.
- Value objects are immutable. Use Kotlin data classes where appropriate.
- Repository interfaces are defined in domain — implementations live in `infra/`.
- Domain events extend a common marker interface from `shared/events/`.

### API Layer (`api/`)
- REST controllers use Spring Web annotations.
- DTOs are separate from domain objects — map explicitly, never expose domain entities directly.
- Domain validation is authoritative — domain classes enforce their own invariants.
- Command objects bridge API → domain.

### Infra Layer (`infra/`)
- JPA entities are separate from domain entities — map between them.
- Spring `ApplicationEventPublisher` for domain event publishing.
- Repository implementations use Spring Data JPA.

### Naming
- Classes: `PascalCase` — match the glossary term (e.g., `Book`, `Loan`, `CopyAvailabilityChanged`)
- Packages: `com.library.<context>.<layer>` (e.g., `com.library.catalog.domain`)
- Test classes: `<ClassName>Test` for unit tests, `<Feature>StepDefs` for Cucumber step definitions

## Testing Conventions

| Type | Location | Naming | Runner |
|---|---|---|---|
| Domain unit tests | `<context>/src/test/kotlin/.../domain/` | `<ClassName>Test.kt` | JUnit 5 |
| BDD acceptance tests | `<context>/src/test/kotlin/.../bdd/` | `<Feature>StepDefs.kt` | Cucumber + JUnit 5 |
| Architecture tests | `<context>/src/test/kotlin/.../arch/` | `<Context>ArchitectureTest.kt` | ArchUnit |

### Architecture Test Rules (ArchUnit)
- Domain layer must not depend on API or Infra layers
- Domain layer must not import any Spring framework classes
- Catalog module must not depend on Lending module (and vice versa)
- Only the `:shared` module may be referenced by both contexts

## How to Implement a Story

Use `/implement-story <NNN> <epic>` — the full workflow is in `.claude/skills/implement-story.md`.

**If anything in the spec is ambiguous: STOP and ask. Do not guess.**

## Parallel Work Rules

- Different bounded contexts can be worked on in parallel; same context must be sequential
- Never modify `shared/events` without checking both contexts

## Build & Run

```bash
./gradlew clean build          # Build everything
./gradlew test                 # Run tests only
./gradlew :application:bootRun # Run the application
./gradlew :catalog:test        # Run a specific context's tests
```

## Skills

| Skill | Usage | Purpose |
|---|---|---|
| `implement-story` | `/implement-story <NNN> <epic>` | Full domain-first story implementation workflow |
| `new-story` | `/new-story <epic> "<title>"` | Scaffold a new `.feature` file and backlog entry |
| `new-adr` | `/new-adr "<title>"` | Create a numbered ADR from the project template |
| `update-backlog` | `/update-backlog <NNN> <epic> <status>` | Move a story between Ready / In Progress / Done |
| `verify-architecture` | `/verify-architecture [epic]` | Run ArchUnit + grep checks for layer violations |
| `write-domain-layer` | `/write-domain-layer <epic> "<description>"` | Entities, value objects, commands, events, ports |
| `write-bdd-tests` | `/write-bdd-tests <epic> <NNN>` | Cucumber step definitions wired via MockMvc |
| `write-backend-adapters` | `/write-backend-adapters <epic> "<description>"` | REST controller + DTOs + JPA entity + repo adapter |
| `write-frontend-page` | `/write-frontend-page "<description>"` | React page + hook + api client following ADRs 005–006 |

## ADR Index

| # | Decision | Status |
|---|---|---|
| [001](docs/decisions/adr-001-project-structure.md) | Project structure and directory layout | Accepted |
| [002](docs/decisions/adr-002-tech-stack.md) | Kotlin + Spring Boot backend, React + Vite frontend, PostgreSQL | Accepted |
| [003](docs/decisions/adr-003-frontend-framework.md) | React with TypeScript + Vite | Accepted |
| [004](docs/decisions/adr-004-frontend-styling-framework.md) | Tailwind CSS | Accepted |
| [005](docs/decisions/adr-005-frontend-api-client-layer.md) | Centralized `src/api/` module with `apiFetch` base function | Accepted |
| [006](docs/decisions/adr-006-frontend-custom-hooks.md) | Extract page logic into co-located `use<Feature>.ts` hooks | Proposed |
| [007](docs/decisions/adr-007-persistent-database.md) | PostgreSQL for persistent storage (replaces H2 in-memory) | Accepted |
