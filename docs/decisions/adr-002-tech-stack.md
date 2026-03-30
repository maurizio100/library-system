# ADR-002: Tech Stack

## Status
Accepted

## Context
The library system needs a backend, frontend, and persistence layer supporting DDD patterns, strong typing, and testability. The team has JVM and TypeScript experience.

## Options Considered
- **Java / Spring Boot** — Mature DDD ecosystem but verbose syntax and boilerplate-heavy.
- **Kotlin / Spring Boot** — Same Spring ecosystem with concise syntax, null safety, and expressive domain modeling.
- **TypeScript / Node.js** — Single language full-stack but weaker DDD ecosystem and less strict typing for domain invariants.

## Decision
**Kotlin / Spring Boot** — best balance of DDD alignment (Spring Data, domain events, data classes for modeling), maintainability (null safety, strong typing), and team familiarity (JVM experience transfers directly).

## Consequences
- Concise domain models via data classes and sealed classes
- Null safety at compile time
- Full access to Spring Boot ecosystem (web, data, test)
- Kotlin learning curve for Java-only developers; some Spring docs are Java-first
