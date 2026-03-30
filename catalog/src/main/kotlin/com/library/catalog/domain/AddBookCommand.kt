package com.library.catalog.domain

data class AddBookCommand(
    val isbn: ISBN,
    val title: String,
    val authors: List<Author>,
    val publicationYear: Int
)
