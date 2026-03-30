package com.library.catalog.api.dto

data class BookSearchResponse(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int,
    val totalCopies: Int,
    val availableCopies: Int
)
