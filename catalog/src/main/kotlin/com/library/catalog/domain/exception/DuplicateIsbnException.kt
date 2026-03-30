package com.library.catalog.domain.exception

import com.library.catalog.domain.model.ISBN

class DuplicateIsbnException(isbn: ISBN) : RuntimeException("ISBN already exists")
