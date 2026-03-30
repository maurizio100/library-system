package com.library.catalog.api

import com.library.catalog.domain.AddBookCommand
import com.library.catalog.domain.AddBookHandler
import com.library.catalog.domain.Author
import com.library.catalog.domain.ISBN
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val addBookHandler: AddBookHandler,
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
        return BookResponse(
            isbn = event.isbn,
            title = event.title,
            authors = event.authors,
            publicationYear = event.publicationYear
        )
    }
}
