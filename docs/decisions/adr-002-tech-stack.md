# ADR-002: Tech Stack

## Status
Proposed

## Context
The library system is a full-stack web application that needs a backend, frontend, and persistence layer. The tech stack must support:
- Domain-driven design patterns (aggregates, repositories, value objects, domain events)
- Long-term maintainability through strong typing, testability, and clear module boundaries
- Team familiarity — the team has experience with JVM-based stacks and TypeScript

The choice of tech stack will shape every subsequent implementation decision, so it must be made deliberately before any code is written.

## Options Considered

### Option A: Java / Spring Boot
Mature enterprise ecosystem with first-class DDD support. Spring Data, Spring MVC, and the broader Spring ecosystem provide batteries-included solutions for web applications. Strong typing via Java's type system. Well-understood deployment and operations story.

**Pros:** Largest ecosystem of DDD libraries and patterns; mature tooling; strong community; excellent testability with JUnit/Mockito; proven at scale.
**Cons:** Verbose syntax; slower developer feedback loop compared to interpreted languages; boilerplate-heavy without Lombok or records.

### Option B: Kotlin / Spring Boot
Modern JVM language that runs on the same Spring Boot ecosystem but with significantly less boilerplate. Data classes, sealed classes, and null safety make domain modeling more expressive. Full interoperability with Java libraries.

**Pros:** Concise, expressive syntax ideal for domain modeling; null safety reduces runtime errors; coroutines for async where needed; full access to Java/Spring ecosystem; excellent testability.
**Cons:** Smaller community than Java (though growing); some Spring features lag behind Java support; additional learning curve for Java-only developers.

### Option C: TypeScript / Node.js
Full-stack JavaScript with type safety. Enables sharing types between frontend and backend. Fast iteration cycle with hot-reload. Frameworks like NestJS offer structure for DDD-style architectures.

**Pros:** Single language across frontend and backend; fast development cycle; large npm ecosystem; good for rapid prototyping.
**Cons:** Weaker DDD ecosystem compared to JVM; TypeScript's structural typing is less strict for domain invariants; Node.js single-threaded model can complicate certain patterns; ORM/persistence tooling less mature for complex domain models.

## Decision
We will use **Kotlin / Spring Boot** because it provides the best balance of our driving forces:
- **DDD alignment:** Full access to the mature Java/Spring DDD ecosystem (Spring Data, domain event publishing, repository abstractions) combined with Kotlin's data classes and sealed classes for expressive domain modeling.
- **Long-term maintainability:** Null safety, strong static typing, and concise syntax reduce defect density and keep the codebase readable as it grows.
- **Team familiarity:** The team's JVM experience transfers directly; Kotlin's learning curve from Java is gentle, and all existing Java libraries remain available.

## Consequences

### Positive
- Domain models will be concise and expressive using data classes, sealed classes, and value classes
- Null safety enforced at compile time reduces a whole category of runtime errors
- Full access to Spring Boot's mature web, security, and data modules
- Strong testability with JUnit 5, MockK, and Spring's test support

### Negative
- Team members without Kotlin experience will need ramp-up time
- Some Spring documentation and examples are Java-first, requiring mental translation
- Build times may be slightly longer than pure Java due to the Kotlin compiler

### Neutral
- Frontend technology is not decided by this ADR — a separate decision will cover the frontend framework
- Gradle will likely be the build tool (idiomatic for Kotlin), but this is confirmed in a later ADR

## References
- [ADR-001: Project Structure](adr-001-project-structure.md)
- [Kotlin Language Reference](https://kotlinlang.org/docs/reference/)
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
