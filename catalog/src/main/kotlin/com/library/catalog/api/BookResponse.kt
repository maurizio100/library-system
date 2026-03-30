package com.library.catalog.api

data class BookResponse(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int
)
