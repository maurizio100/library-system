package com.library.catalog.domain

class Book(
    val isbn: ISBN,
    val title: String,
    val authors: List<Author>,
    val publicationYear: Int
) {
    init {
        require(title.isNotBlank()) { "Title is required" }
        require(authors.isNotEmpty()) { "Author is required" }
    }

    companion object {
        fun create(isbn: ISBN, title: String, authors: List<Author>, publicationYear: Int): Pair<Book, BookAdded> {
            val book = Book(isbn, title, authors, publicationYear)
            val event = BookAdded(
                isbn = isbn.value,
                title = title,
                authors = authors.map { it.name },
                publicationYear = publicationYear
            )
            return book to event
        }
    }
}
