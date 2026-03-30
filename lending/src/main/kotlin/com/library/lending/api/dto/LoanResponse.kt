package com.library.lending.api.dto

import java.time.LocalDate

data class LoanResponse(
    val loanId: String,
    val memberId: String,
    val copyBarcode: String,
    val loanDate: LocalDate,
    val dueDate: LocalDate,
    val status: String
)
