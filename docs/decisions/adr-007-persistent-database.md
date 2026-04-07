# ADR-007: Persistent Database Storage

## Status
Proposed

## Context
The application currently uses H2 in-memory mode (`jdbc:h2:mem:librarydb`) with `ddl-auto: create-drop`.
This means all data is lost every time the application restarts, which makes it impossible to work
with realistic, accumulated data during development and testing.

A persistent database is needed so that catalog and lending data survives application restarts.

## Options Considered

### Option A: H2 File-Based
Switch from `jdbc:h2:mem:` to `jdbc:h2:file:` — H2 writes data to a local file instead of memory.
No new dependencies or services required; just a URL change and setting `ddl-auto: update`.

**Pros:**
- Zero additional setup — H2 is already on the classpath
- Persistence is immediate with a one-line config change

**Cons:**
- Not a production-grade database
- File-based H2 has known edge cases with concurrent connections
- Limited tooling for schema migrations

### Option B: PostgreSQL
Replace H2 with PostgreSQL, the most common relational database for Spring Boot applications in production.
Requires a local PostgreSQL instance or Docker.

**Pros:**
- Production-grade — matches what the app would use in a real deployment
- Full SQL compliance, robust concurrency, and mature tooling
- Works naturally with Flyway for schema migrations

**Cons:**
- Requires a running PostgreSQL server (local install or `docker compose up`)
- Additional dependency (`postgresql` JDBC driver) and configuration changes

## Decision
We will use **PostgreSQL** because it is production-grade, widely used with Spring Boot,
and gives us a realistic persistence layer that matches what the application would use in
a real deployment. The in-memory H2 setup will be removed entirely.

A `docker-compose.yml` will be provided so developers can spin up PostgreSQL locally without
a manual installation.

## Consequences

### Positive
- Data persists across application restarts
- Realistic development environment that mirrors production
- Schema migration tooling (e.g. Flyway) can be introduced when needed

### Negative
- Developers need Docker (or a local PostgreSQL install) to run the application
- More moving parts than H2 in-memory

### Neutral
- The H2 dependency and H2 console will be removed from the project
- `ddl-auto` should be set to `validate` or managed via Flyway rather than `create-drop`

## References
- [ADR-002: Tech Stack](adr-002-tech-stack.md)
