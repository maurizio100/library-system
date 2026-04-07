package com.library.catalog.api.dto

data class AddBookRequest(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int,
    val coverUrl: String? = null
)
