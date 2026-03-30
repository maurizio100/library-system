package com.library.catalog.domain

class BookNotFoundException(isbn: ISBN) : RuntimeException("Book not found")
