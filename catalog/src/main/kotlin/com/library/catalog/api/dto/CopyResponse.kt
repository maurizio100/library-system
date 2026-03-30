package com.library.catalog.api.dto

data class CopyResponse(
    val barcode: String,
    val isbn: String,
    val status: String
)
