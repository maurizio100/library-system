## Core Patterns

| Pattern | Rationale |
|---|---|
| **Hexagonal Architecture** | Keeps domain logic pure and testable. Framework dependencies stay in adapters. |
| **Domain-Driven Design** | Two bounded contexts (Catalog, Lending) with explicit boundaries and a shared glossary. |
| **Modular Monolith** | Bounded contexts as Maven modules. Simpler than microservices, still enforces isolation. |
| **Domain Events** | Inter-context communication via Spring's event system. Loose coupling between Catalog and Lending. |
| **BDD with Cucumber** | Gherkin stories are executable specs. Acceptance criteria are automated, not just documentation. |
| **Architecture Fitness Functions** | ArchUnit tests enforce hexagonal layer rules and bounded context isolation at build time. |

## Testing Strategy

| Layer | Tool | What It Tests |
|---|---|---|
| Domain unit tests | JUnit 5 | Entity invariants, value object validation, business rules |
| BDD acceptance tests | Cucumber + JUnit 5 | Gherkin scenarios from `.feature` files |
| Architecture tests | ArchUnit | Layer dependencies, no domain → infra imports, context isolation |
| Integration tests | Spring Boot Test | REST endpoints, persistence, event publishing |

## Build & Run
```bash
# Build everything
mvn clean verify

# Run tests only
mvn test

# Run the application
mvn spring-boot:run -pl application

# Run a specific bounded context's tests
mvn test -pl catalog
mvn test -pl lending
```