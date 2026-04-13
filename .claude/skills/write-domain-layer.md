# Skill: write-domain-layer

Implement the domain layer for a feature: entities, value objects, commands, events, exceptions, and repository interfaces.

## Usage

```
/write-domain-layer <epic> "<feature description>"
```

Example:
- `/write-domain-layer lending "suspend a member"`

## Before you start

- Read the `.feature` file to understand the scenarios
- Read `docs/domain/bounded-contexts/<epic>.md` for invariants and aggregate rules
- Read `docs/domain/glossary.md` — use exact names, no paraphrasing

---

## Rules (non-negotiable)

- **No Spring imports anywhere in `domain/`** — pure Kotlin only
- Value objects and entities must enforce their own invariants
- Repository interfaces belong in `domain/port/` — implementations in `infra/`
- Domain events extend the shared marker interface from `:shared`
- Use the command pattern: one `<Action>Command` data class + one `<Action>Handler` class per operation

---

## File layout

```
<epic>/src/main/kotlin/com/library/<epic>/domain/
├── model/          ← entities and value objects
├── command/        ← command data classes and handlers
├── event/          ← domain events
├── exception/      ← domain exceptions
└── port/           ← repository interfaces
```

---

## Value objects

Enforce invariants in `init` with `require()`. Use a plain class (not `data class`) when the value needs custom `equals`/`hashCode` based on a normalised form (e.g. ISBN strips hyphens).

```kotlin
class ISBN(input: String) {
    val value: String = run {
        require(input.matches(Regex("^[\\d-]+$"))) { "ISBN may only contain digits and hyphens" }
        val digits = input.replace("-", "")
        require(digits.length == 13) { "ISBN must contain exactly 13 digits (got ${digits.length})" }
        digits
    }
    override fun equals(other: Any?) = other is ISBN && value == other.value
    override fun hashCode() = value.hashCode()
}
```

Use `data class` when all fields are meaningful and equality is structural:

```kotlin
data class Author(val name: String) {
    init { require(name.isNotBlank()) { "Author is required" } }
}
```

---

## Entities

- Identity field typed as a value object
- Invariants enforced in `init`
- Mutating operations return domain events
- Use a `companion object { fun create(...): Pair<Entity, Event> }` factory for creation events

```kotlin
class Book(
    val isbn: ISBN,
    val title: String,
    val authors: List<Author>,
    val publicationYear: Int,
    internal val _copies: MutableList<Copy> = mutableListOf()
) {
    val copies: List<Copy> get() = _copies.toList()

    init {
        require(title.isNotBlank()) { "Title is required" }
        require(authors.isNotEmpty()) { "Author is required" }
    }

    fun registerCopy(barcode: Barcode): CopyRegistered {
        if (_copies.any { it.barcode == barcode }) throw DuplicateBarcodeException(barcode)
        _copies.add(Copy(barcode))
        return CopyRegistered(isbn = isbn.value, barcode = barcode.value)
    }

    companion object {
        fun create(isbn: ISBN, title: String, authors: List<Author>, publicationYear: Int): Pair<Book, BookAdded> {
            val book = Book(isbn, title, authors, publicationYear)
            return book to BookAdded(isbn = isbn.value, title = title, authors = authors.map { it.name }, publicationYear = publicationYear)
        }
    }
}
```

---

## Commands and handlers

One data class per command, one handler class per command. Handlers receive repository ports via constructor injection (no Spring annotations on the handler class itself — Spring wiring happens in the infra/config layer).

```kotlin
data class RegisterCopyCommand(val isbn: ISBN, val barcode: Barcode)

class RegisterCopyHandler(private val bookRepository: BookRepository) {
    fun handle(command: RegisterCopyCommand): CopyRegistered {
        val book = bookRepository.findByIsbn(command.isbn) ?: throw BookNotFoundException(command.isbn)
        val event = book.registerCopy(command.barcode)
        bookRepository.save(book)
        return event
    }
}
```

---

## Domain events

Extend the shared marker interface. Use raw primitive types (String, Int) — no domain types — so events can cross context boundaries via `:shared`.

```kotlin
// in shared module
interface DomainEvent

// in catalog domain
data class CopyRegistered(val isbn: String, val barcode: String) : DomainEvent
```

---

## Exceptions

One class per distinct failure. Message must match the exact wording expected in the `.feature` file scenarios.

```kotlin
class DuplicateBarcodeException(barcode: Barcode) : RuntimeException("Barcode already exists: ${barcode.value}")
class BookNotFoundException(isbn: ISBN) : RuntimeException("Book not found: ${isbn.value}")
```

---

## Repository interfaces

Defined in `domain/port/`. Operate on domain types — no JPA, no Spring.

```kotlin
interface BookRepository {
    fun findByIsbn(isbn: ISBN): Book?
    fun findByBarcode(barcode: Barcode): Book?
    fun save(book: Book)
    fun existsByIsbn(isbn: ISBN): Boolean
}
```

---

## Checklist before handing off

- [ ] No Spring import anywhere under `domain/`
- [ ] All invariants covered by `require()` with messages that match the Gherkin `Then` steps
- [ ] Every mutating operation returns a domain event (or throws)
- [ ] Repository interface added to `domain/port/`
- [ ] Exceptions have human-readable messages matching the `.feature` file
