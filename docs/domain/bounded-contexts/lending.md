
## Purpose

Manages the lending operations — members, loans, returns, and overdue fee calculation. Owns the truth about who has borrowed what and what they owe.

## Key Entities

### Member (Aggregate Root)
- **Identity:** Member ID (generated UUID)
- **Attributes:** name, email, borrowing limit (default: 3), active loans count
- **Invariants:**
  - Cannot have more active loans than the borrowing limit
  - Email must be valid and unique across members

### Loan (Aggregate Root)
- **Identity:** Loan ID (generated UUID)
- **Attributes:** member ID, copy barcode, loan date, due date, return date (nullable), status (Active | Returned | Overdue)
- **Invariants:**
  - A Loan must reference an existing Member and a valid Copy barcode
  - Due date = loan date + loan period (14 days)
  - A Loan can only be returned once
  - Status transitions: Active → Returned, Active → Overdue, Overdue → Returned

## Value Objects

| Value Object | Attributes | Validation Rules |
|---|---|---|
| **MemberId** | value (UUID) | Must be valid UUID |
| **LoanId** | value (UUID) | Must be valid UUID |
| **LoanPeriod** | days (int) | Must be positive, default 14 |
| **Fee** | amount (BigDecimal), currency (String) | Amount must be ≥ 0, currency defaults to EUR |
| **DailyRate** | amount (BigDecimal) | Must be positive, default €0.50 |

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
| **CreateLoan** | Member exists, has capacity (active loans < limit), Copy is Available | Creates Loan, notifies Catalog to mark copy as Borrowed |
| **ReturnBook** | Loan exists and is Active or Overdue | Closes the Loan, calculates Fee if overdue, notifies Catalog to mark copy as Available |
| **DetectOverdueLoans** | Scheduled / triggered by date change | Finds all Active Loans past due date, marks them Overdue, publishes LoanOverdue events |

## Context Relationships

- **Upstream dependency:** Catalog context — Lending consumes `CopyAvailabilityChanged` events and queries copy availability
- **Integration with Catalog on loan creation:** When a Loan is created, Lending sends a command (or event) to Catalog to mark the Copy as Borrowed
- **Integration with Catalog on return:** When a Book is returned, Lending sends a command (or event) to Catalog to mark the Copy as Available