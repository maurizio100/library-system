package com.library.catalog.api.dto

data class BookResponse(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int
)
