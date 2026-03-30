## Business Context

A small public library wants to digitize its lending operations. Currently everything is tracked on paper.

The system must support:
- **Librarians** who manage the catalog (add books, register copies) and handle lending operations
- **Members** who borrow and return books

Scale: ~500 active members, ~5,000 books, ~8,000 physical copies.

## Constraints

- Must run as a single deployable unit (no microservices infrastructure available)
- No external message broker — domain events are in-process only
- The library has no IT staff — the system must be simple to operate

## Future Integrations

- **ISBN lookup service** (e.g., Open Library API) for auto-populating book metadata
- **Email service** for overdue notifications
