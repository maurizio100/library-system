# Skill: write-backend-adapters

Implement the API layer (controller, DTOs, exception handler) and the infra layer (JPA entities, repository adapter) for a feature.

## Usage

```
/write-backend-adapters <epic> "<feature description>"
```

Example:
- `/write-backend-adapters lending "suspend a member"`

## Before you start

- The domain layer must already exist (`domain/model/`, `domain/command/`, `domain/port/`)
- Read the `.feature` file — the HTTP status codes and error messages in `Then` steps are authoritative
- Read `docs/domain/bounded-contexts/<epic>.md` for any REST path conventions

---

## API layer

### File layout

```
<epic>/src/main/kotlin/com/library/<epic>/api/
├── controller/
│   ├── <Context>Controller.kt
│   └── <Context>ExceptionHandler.kt
└── dto/
    ├── <Action>Request.kt
    └── <Action>Response.kt
```

### Controller conventions

- Single controller per bounded context: `@RestController @RequestMapping("/api/<epic>")`
- Inject command handlers and repository ports via constructor
- Map request DTO → command → call handler → publish event → map result → response DTO
- Never expose domain entities directly in responses
- Publish domain events with `ApplicationEventPublisher`

```kotlin
@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val addBookHandler: AddBookHandler,
    private val bookRepository: BookRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    fun addBook(@RequestBody request: AddBookRequest): BookResponse {
        val command = AddBookCommand(
            isbn = ISBN(request.isbn),
            title = request.title,
            authors = request.authors.map { Author(it) },
            publicationYear = request.publicationYear
        )
        val event = addBookHandler.handle(command)
        eventPublisher.publishEvent(event)
        return BookResponse(isbn = event.isbn, title = event.title, authors = event.authors, publicationYear = event.publicationYear)
    }
}
```

### DTOs

Plain `data class` with primitive/String fields only. No domain types.

```kotlin
data class AddBookRequest(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int,
    val coverUrl: String? = null
)

data class BookResponse(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int
)
```

### Exception handler

One `@RestControllerAdvice` per bounded context, scoped to `basePackages = ["com.library.<epic>"]`.

Map each domain exception to the appropriate HTTP status. The error message must match what the `.feature` file expects in `Then` steps.

```kotlin
@RestControllerAdvice(basePackages = ["com.library.catalog"])
class CatalogExceptionHandler {

    @ExceptionHandler(DuplicateIsbnException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateIsbn(ex: DuplicateIsbnException) = ErrorResponse(ex.message ?: "ISBN already exists")

    @ExceptionHandler(BookNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: BookNotFoundException) = ErrorResponse(ex.message ?: "Book not found")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: IllegalArgumentException) = ErrorResponse(ex.message ?: "Invalid request")
}

data class ErrorResponse(val error: String)
```

HTTP status mapping guide:
| Situation | Status |
|---|---|
| Duplicate / already exists | 409 Conflict |
| Not found | 404 Not Found |
| Invalid input / invariant violation | 400 Bad Request |
| External service unavailable | 503 Service Unavailable |
| Cannot perform action (e.g. copy borrowed) | 409 Conflict |

---

## Infra layer

### File layout

```
<epic>/src/main/kotlin/com/library/<epic>/infra/persistence/
├── <Entity>JpaEntity.kt
├── <Entity>JpaRepository.kt
└── <Entity>RepositoryAdapter.kt
```

### JPA entities

Separate from domain entities. Use JPA annotations freely here.

```kotlin
@Entity
@Table(name = "books")
class BookJpaEntity(
    @Id val isbn: String = "",
    val title: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    val authors: MutableList<String> = mutableListOf(),
    val publicationYear: Int = 0,
    val coverUrl: String? = null
)
```

### Spring Data JPA repositories

```kotlin
interface BookJpaRepository : JpaRepository<BookJpaEntity, String>
```

### Repository adapter

Implements the domain port. Contains `toDomain()` and `toJpaEntity()` private extension functions.

```kotlin
@Repository
class BookRepositoryAdapter(
    private val bookJpaRepository: BookJpaRepository,
    private val copyJpaRepository: CopyJpaRepository
) : BookRepository {

    override fun findByIsbn(isbn: ISBN): Book? {
        val entity = bookJpaRepository.findById(isbn.value).orElse(null) ?: return null
        val copies = copyJpaRepository.findByIsbn(isbn.value)
        return entity.toDomain(copies)
    }

    override fun save(book: Book) {
        bookJpaRepository.save(book.toJpaEntity())
        // sync child collections explicitly if needed
    }

    private fun BookJpaEntity.toDomain(copies: List<CopyJpaEntity>): Book { ... }
    private fun Book.toJpaEntity(): BookJpaEntity { ... }
}
```

### Command handler Spring wiring

Command handlers live in `domain/` with no Spring annotations. Wire them as `@Bean` in a config class in `infra/`:

```kotlin
@Configuration
class CatalogConfig(private val bookRepository: BookRepository) {
    @Bean fun addBookHandler() = AddBookHandler(bookRepository)
    @Bean fun registerCopyHandler() = RegisterCopyHandler(bookRepository)
}
```

---

## Checklist before committing

- [ ] DTOs contain no domain types
- [ ] Every domain exception is handled in the `@RestControllerAdvice`
- [ ] Error messages in exception handler match the `.feature` file wording
- [ ] JPA entity is separate from the domain entity (no `@Entity` on domain classes)
- [ ] `toDomain()` and `toJpaEntity()` mappings are complete and symmetrical
- [ ] Command handlers wired as Spring `@Bean` in a config class, not annotated directly
- [ ] Run `./gradlew :<epic>:test` — all tests green
