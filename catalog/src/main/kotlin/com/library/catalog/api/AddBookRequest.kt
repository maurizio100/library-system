package com.library.catalog.api

data class AddBookRequest(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int
)
