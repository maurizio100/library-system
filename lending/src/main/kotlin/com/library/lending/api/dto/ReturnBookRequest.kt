package com.library.lending.api.dto

data class ReturnBookRequest(
    val memberId: String,
    val copyBarcode: String
)
