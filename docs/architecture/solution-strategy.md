## Core Patterns

| Pattern | Rationale |
|---|---|
| **Hexagonal Architecture** | Keeps domain logic pure and testable. Framework dependencies stay in adapters. |
| **Domain-Driven Design** | Two bounded contexts (Catalog, Lending) with explicit boundaries and a shared glossary. |
| **Modular Monolith** | Bounded contexts as Gradle modules. Simpler than microservices, still enforces isolation. |
| **Domain Events** | Inter-context communication via Spring's event system. Loose coupling between Catalog and Lending. |
| **BDD with Cucumber** | Gherkin stories are executable specs. Acceptance criteria are automated, not just documentation. |
| **Architecture Fitness Functions** | ArchUnit tests enforce hexagonal layer rules and bounded context isolation at build time. |
| **Spring Data JPA Repositories** | Prefer derived query methods and `@Query` JPQL over native SQL. Keeps persistence code concise, type-safe, and database-agnostic. |
