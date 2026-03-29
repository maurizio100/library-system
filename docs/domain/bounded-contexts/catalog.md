## Purpose

Manages the library's collection of books and their physical copies. Owns the truth about what the library has and whether copies are available for lending.

## Key Entities

### Book (Aggregate Root)
- **Identity:** ISBN
- **Attributes:** title, author(s), publication year
- **Invariants:**
  - ISBN must be valid (13 digits)
  - A Book must have at least a title and one author
- **Owns:** a collection of Copies

### Copy (Entity, child of Book)
- **Identity:** Barcode
- **Attributes:** barcode, status (Available | Borrowed)
- **Invariants:**
  - Barcode must be unique across all Copies in the system
  - Status transitions: Available → Borrowed, Borrowed → Available

## Value Objects

| Value Object | Attributes | Validation Rules |
|---|---|---|
| **ISBN** | value (String) | Must be exactly 13 digits |
| **Author** | name (String) | Must not be blank |
| **Barcode** | value (String) | Must not be blank, must be unique |

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

- **Downstream consumer:** Lending context subscribes to `CopyAvailabilityChanged` events
- **Inbound queries:** Lending context may query copy availability before creating a loan