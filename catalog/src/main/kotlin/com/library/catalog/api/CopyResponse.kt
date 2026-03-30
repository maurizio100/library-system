package com.library.catalog.api

data class CopyResponse(
    val barcode: String,
    val isbn: String,
    val status: String
)
