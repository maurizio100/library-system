package com.library.catalog.domain.command

import com.library.catalog.domain.event.BookAdded
import com.library.catalog.domain.exception.DuplicateIsbnException
import com.library.catalog.domain.model.Book
import com.library.catalog.domain.port.BookRepository

class AddBookHandler(private val bookRepository: BookRepository) {

    fun handle(command: AddBookCommand): BookAdded {
        if (bookRepository.existsByIsbn(command.isbn)) {
            throw DuplicateIsbnException(command.isbn)
        }
        val (book, event) = Book.create(
            isbn = command.isbn,
            title = command.title,
            authors = command.authors,
            publicationYear = command.publicationYear,
            coverUrl = command.coverUrl
        )
        bookRepository.save(book)
        return event
    }
}
