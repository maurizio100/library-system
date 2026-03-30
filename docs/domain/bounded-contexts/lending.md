## Purpose

Manages the lending operations — members, loans, returns, and overdue fee calculation. Owns the truth about who has borrowed what and what they owe.

## Aggregates & Invariants

### Member (Aggregate Root) — identity: Member ID (UUID)
- Cannot have more active loans than the borrowing limit (default: 3)
- Email must be valid and unique across members

### Loan (Aggregate Root) — identity: Loan ID (UUID)
- Must reference an existing Member and a valid Copy barcode
- Due date = loan date + 14 days
- A Loan can only be returned once
- Status transitions: Active → Returned, Active → Overdue, Overdue → Returned
- Overdue fee: days overdue × €0.50/day

## Domain Events

| Event | Trigger | Payload |
|---|---|---|
| **MemberRegistered** | A new Member signs up | memberId, name, email |
| **LoanCreated** | A Member borrows a Copy | loanId, memberId, copyBarcode, loanDate, dueDate |
| **BookReturned** | A Member returns a Copy | loanId, memberId, copyBarcode, returnDate |
| **LoanOverdue** | System detects a Loan past its due date | loanId, memberId, copyBarcode, daysOverdue |
| **FeeCharged** | An overdue fee is calculated upon return | loanId, memberId, amount, daysOverdue |

## Commands

| Command | Preconditions | Effect |
|---|---|---|
| **RegisterMember** | Email is not already registered | Creates a new Member |
| **CreateLoan** | Member exists, has capacity, Copy is Available | Creates Loan, notifies Catalog to mark copy as Borrowed |
| **ReturnBook** | Loan exists and is Active or Overdue | Closes the Loan, calculates Fee if overdue, notifies Catalog to mark copy as Available |
| **DetectOverdueLoans** | Scheduled / triggered by date change | Finds all Active Loans past due date, marks them Overdue |

## Context Relationships

- **Upstream dependency:** Catalog — Lending consumes `CopyAvailabilityChanged` events and queries copy availability
- **On loan creation:** Lending notifies Catalog to mark the Copy as Borrowed
- **On return:** Lending notifies Catalog to mark the Copy as Available
