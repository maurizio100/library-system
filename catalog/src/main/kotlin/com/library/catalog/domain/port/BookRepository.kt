package com.library.catalog.domain.port

import com.library.catalog.domain.model.Book
import com.library.catalog.domain.model.ISBN

interface BookRepository {
    fun findByIsbn(isbn: ISBN): Book?
    fun save(book: Book)
    fun existsByIsbn(isbn: ISBN): Boolean
}
