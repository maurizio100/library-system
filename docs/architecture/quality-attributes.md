
| Priority | Attribute | Scenario |
|---|---|---|
| 1 | **Correctness** | Loan creation must enforce borrowing limits — a member with 3 active loans (limit: 3) must be rejected. Zero tolerance for invariant violations. |
| 2 | **Testability** | Every business rule must be verifiable via a Gherkin scenario. Domain logic must be testable without Spring context. |
| 3 | **Modularity** | Bounded contexts must be independently testable. No compile-time dependency from Catalog to Lending or vice versa (only via shared events module). |
| 4 | **Simplicity** | A developer (or agent) should be able to understand where to add a new feature by reading CLAUDE.md and the relevant bounded context doc. |
| 5 | **Performance** | Not a primary concern for this scale. Response times under 500ms for all operations are sufficient. |
