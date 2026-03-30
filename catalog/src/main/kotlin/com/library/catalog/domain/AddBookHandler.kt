package com.library.catalog.domain

class AddBookHandler(private val bookRepository: BookRepository) {

    fun handle(command: AddBookCommand): BookAdded {
        if (bookRepository.existsByIsbn(command.isbn)) {
            throw DuplicateIsbnException(command.isbn)
        }
        val (book, event) = Book.create(
            isbn = command.isbn,
            title = command.title,
            authors = command.authors,
            publicationYear = command.publicationYear
        )
        bookRepository.save(book)
        return event
    }
}
