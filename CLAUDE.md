# Library System

A modular monolith for managing a public library's catalog and lending operations. Built with Kotlin, Spring Boot 3.x, React, and Gradle.

## Directory Layout

```
├── CLAUDE.md              ← you are here
├── Backlog.md             ← stories: Ready / In Progress / Done
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
│   ├── decisions/          ← ADRs
│   ├── skills/             ← project-level agent skills
│   └── stories/
│       ├── catalog/        ← .feature files for Catalog epic
│       └── lending/        ← .feature files for Lending epic
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

| Context | Gradle Module | REST Base Path | Spec |
|---|---|---|---|
| Catalog | `:catalog` | `/api/catalog` | [docs/domain/bounded-contexts/catalog.md](docs/domain/bounded-contexts/catalog.md) |
| Lending | `:lending` | `/api/lending` | [docs/domain/bounded-contexts/lending.md](docs/domain/bounded-contexts/lending.md) |

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

1. Read the `.feature` file for the story
2. Read the relevant bounded context spec in `docs/domain/bounded-contexts/`
3. Read the glossary at `docs/domain/glossary.md`
4. Create a branch: `story/<NNN>-<short-name>`
5. **Implement domain first** — entities, value objects, business rules
6. **Write tests** — Cucumber step definitions for every scenario, unit tests for domain logic
7. **Then API and infra** — controllers, DTOs, persistence
8. **Run full verification:** `./gradlew clean build`
9. All Gherkin scenarios must pass before committing
10. Commit: `feat(<context>): implement story <NNN> — <short title>`
11. Open PR with story reference

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
