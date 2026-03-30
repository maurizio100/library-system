## Purpose

Manages the library's collection of books and their physical copies. Owns the truth about what the library has and whether copies are available for lending.

## Aggregates & Invariants

### Book (Aggregate Root) — identity: ISBN
- Must have at least a title and one author
- Owns a collection of Copies

### Copy (Entity, child of Book) — identity: Barcode
- Barcode must be unique across all Copies in the system
- Status transitions: Available → Borrowed, Borrowed → Available

## Domain Events

| Event | Trigger | Payload |
|---|---|---|
| **BookAdded** | A new Book is registered in the catalog | isbn, title, authors, publicationYear |
| **CopyRegistered** | A new physical Copy is added to a Book | isbn, barcode |
| **CopyAvailabilityChanged** | A Copy's status changes | barcode, isbn, newStatus (Available/Borrowed) |

## Commands

| Command | Preconditions | Effect |
|---|---|---|
| **AddBook** | ISBN does not already exist in catalog | Creates a new Book |
| **RegisterCopy** | Book with given ISBN exists, barcode is unique | Adds a Copy to the Book |
| **MarkCopyAsBorrowed** | Copy exists and is Available | Changes Copy status to Borrowed, publishes CopyAvailabilityChanged |
| **MarkCopyAsAvailable** | Copy exists and is Borrowed | Changes Copy status to Available, publishes CopyAvailabilityChanged |

## Context Relationships

- **Downstream consumer:** Lending subscribes to `CopyAvailabilityChanged` events
- **Inbound queries:** Lending may query copy availability before creating a loan
