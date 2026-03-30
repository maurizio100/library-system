package com.library.catalog.api.controller

import com.library.catalog.api.dto.AddBookRequest
import com.library.catalog.api.dto.BookResponse
import com.library.catalog.api.dto.BookSearchResponse
import com.library.catalog.api.dto.CopyResponse
import com.library.catalog.api.dto.RegisterCopyRequest
import com.library.catalog.domain.command.AddBookCommand
import com.library.catalog.domain.command.AddBookHandler
import com.library.catalog.domain.command.RegisterCopyCommand
import com.library.catalog.domain.command.RegisterCopyHandler
import com.library.catalog.domain.model.Author
import com.library.catalog.domain.model.Barcode
import com.library.catalog.domain.model.ISBN
import com.library.catalog.domain.port.BookSearchPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val addBookHandler: AddBookHandler,
    private val registerCopyHandler: RegisterCopyHandler,
    private val bookSearchPort: BookSearchPort,
    private val eventPublisher: ApplicationEventPublisher
) {

    @GetMapping("/books")
    fun searchBooks(@RequestParam q: String): List<BookSearchResponse> {
        require(q.isNotBlank()) { "Search query is required" }
        return bookSearchPort.search(q).map {
            BookSearchResponse(
                isbn = it.isbn,
                title = it.title,
                authors = it.authors,
                publicationYear = it.publicationYear,
                availableCopies = it.availableCopies
            )
        }
    }

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
        return BookResponse(
            isbn = event.isbn,
            title = event.title,
            authors = event.authors,
            publicationYear = event.publicationYear
        )
    }

    @PostMapping("/books/{isbn}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerCopy(@PathVariable isbn: String, @RequestBody request: RegisterCopyRequest): CopyResponse {
        val command = RegisterCopyCommand(
            isbn = ISBN(isbn),
            barcode = Barcode(request.barcode)
        )
        val event = registerCopyHandler.handle(command)
        eventPublisher.publishEvent(event)
        return CopyResponse(
            barcode = event.barcode,
            isbn = event.isbn,
            status = "Available"
        )
    }
}
