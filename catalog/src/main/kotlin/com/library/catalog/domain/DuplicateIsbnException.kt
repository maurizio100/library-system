package com.library.catalog.domain

class DuplicateIsbnException(isbn: ISBN) : RuntimeException("ISBN already exists")
