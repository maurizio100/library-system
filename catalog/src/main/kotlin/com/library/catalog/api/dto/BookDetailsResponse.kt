package com.library.catalog.api.dto

data class BookDetailsResponse(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int,
    val copies: List<CopyDetailResponse>
)

data class CopyDetailResponse(
    val barcode: String,
    val status: String
)
