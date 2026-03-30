package com.library.lending.api.dto

data class CreateLoanRequest(
    val memberId: String,
    val copyBarcode: String
)
