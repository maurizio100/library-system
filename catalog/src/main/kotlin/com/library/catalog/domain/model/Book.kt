package com.library.catalog.domain.model

import com.library.catalog.domain.event.BookAdded
import com.library.catalog.domain.event.CopyRegistered
import com.library.catalog.domain.exception.DuplicateBarcodeException

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
        if (_copies.any { it.barcode == barcode }) {
            throw DuplicateBarcodeException(barcode)
        }
        _copies.add(Copy(barcode))
        return CopyRegistered(isbn = isbn.value, barcode = barcode.value)
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
