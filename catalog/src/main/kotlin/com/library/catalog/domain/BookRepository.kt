package com.library.catalog.domain

interface BookRepository {
    fun findByIsbn(isbn: ISBN): Book?
    fun save(book: Book)
    fun existsByIsbn(isbn: ISBN): Boolean
}
