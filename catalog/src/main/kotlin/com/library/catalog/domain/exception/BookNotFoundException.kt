package com.library.catalog.domain.exception

import com.library.catalog.domain.model.ISBN

class BookNotFoundException(isbn: ISBN) : RuntimeException("Book not found")
