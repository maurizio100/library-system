# Library System

A modular monolith for managing a public library's catalog and lending operations. Built with Java 21, Spring Boot 3.x, Angular, and Maven.

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
└── src/                    ← created when first module is implemented
    ├── catalog/
    │   ├── domain/         ← entities, value objects, events, repository interfaces
    │   ├── api/            ← REST controllers, DTOs
    │   ├── infra/          ← JPA repositories, Spring event publishers
    │   └── tests/
    │       ├── bdd/        ← Cucumber step definitions
    │       └── arch/       ← ArchUnit tests
    ├── lending/
    │   └── (same structure)
    └── shared/
        └── events/         ← domain event classes shared between contexts
```

## Bounded Contexts

This system has two bounded contexts. **Never mix code between them.**

| Context | Maven Module | REST Base Path | Spec |
|---|---|---|---|
| Catalog | `catalog` | `/api/catalog` | [docs/domain/bounded-contexts/catalog.md](docs/domain/bounded-contexts/catalog.md) |
| Lending | `lending` | `/api/lending` | [docs/domain/bounded-contexts/lending.md](docs/domain/bounded-contexts/lending.md) |

Cross-context communication happens **only** through domain events in the `shared` module.

## Coding Conventions

### Domain Layer (`domain/`)
- **No Spring imports.** Domain classes must be pure Java — no `@Component`, `@Service`, `@Autowired`, `@Entity`.
- Entities use the names from the [glossary](docs/domain/glossary.md) exactly.
- Value objects are immutable. Use Java records where appropriate.
- Repository interfaces are defined in domain — implementations live in `infra/`.
- Domain events extend a common marker interface from `shared/events/`.

### API Layer (`api/`)
- REST controllers use Spring Web annotations.
- DTOs are separate from domain objects — map explicitly, never expose domain entities directly.
- Input validation happens at the API layer (Bean Validation annotations on DTOs).
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
| Domain unit tests | `src/<context>/tests/` | `<ClassName>Test.java` | JUnit 5 |
| BDD acceptance tests | `src/<context>/tests/bdd/` | `<Feature>StepDefs.java` | Cucumber + JUnit 5 |
| Architecture tests | `src/<context>/tests/arch/` | `<Context>ArchitectureTest.java` | ArchUnit |

### Architecture Test Rules (ArchUnit)
- Domain layer must not depend on API or Infra layers
- Domain layer must not import any Spring framework classes
- Catalog module must not depend on Lending module (and vice versa)
- Only the `shared/events` module may be referenced by both contexts

## How to Implement a Story

1. Read the `.feature` file for the story
2. Read the relevant bounded context spec in `docs/domain/bounded-contexts/`
3. Read the glossary at `docs/domain/glossary.md`
4. Create a branch: `story/<NNN>-<short-name>`
5. **Implement domain first** — entities, value objects, business rules
6. **Write tests** — Cucumber step definitions for every scenario, unit tests for domain logic
7. **Then API and infra** — controllers, DTOs, persistence
8. **Run full verification:** `mvn clean verify`
9. All Gherkin scenarios must pass before committing
10. Commit: `feat(<context>): implement story <NNN> — <short title>`
11. Open PR with story reference

**If anything in the spec is ambiguous: STOP and ask. Do not guess.**

## Parallel Work Rules

- Two stories in **different** bounded contexts can be worked on in parallel
- Two stories in the **same** bounded context must be sequential
- Never modify the `shared/events` module without checking both contexts
- Never modify files outside the story's bounded context

## Build & Run

```bash
# Build everything
mvn clean verify

# Run tests only
mvn test

# Run the application
mvn spring-boot:run -pl application

# Run a specific context's tests
mvn test -pl catalog
mvn test -pl lending
```

## Key Specs

- [Context Map](docs/domain/context-map.md)
- [Glossary](docs/domain/glossary.md)
- [Target Architecture](docs/architecture/target-architecture.md)
- [Solution Strategy](docs/architecture/solution-strategy.md)
- [Quality Attributes](docs/architecture/quality-attributes.md)
- [ADR Index](docs/decisions/)