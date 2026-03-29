## Business Context

A small public library wants to digitize its lending operations. Currently everything is tracked on paper — which books exist, who borrowed what, and which returns are overdue.

The system must support:
- **Librarians** who manage the catalog (add books, register copies) and handle lending operations
- **Members** who borrow and return books

The library serves approximately 500 active members and holds around 5,000 books with roughly 8,000 physical copies.

## Technical Context

### Runtime Environment
- Java 21 on a single application server
- H2 in-memory database for development, PostgreSQL for production (deferred)
- REST API consumed by an Angular single-page application

### Foreign Systems
None currently. The system is self-contained. Future integrations may include:
- **ISBN lookup service** (e.g., Open Library API) for auto-populating book metadata
- **Email service** for overdue notifications

### Constraints
- Must run as a single deployable unit (no microservices infrastructure available)
- No external message broker — domain events are in-process only
- The library has no IT staff — the system must be simple to operate
EOF