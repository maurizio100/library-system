package com.library.lending.api.dto

import java.math.BigDecimal
import java.time.LocalDate

data class ReturnResponse(
    val loanId: String,
    val returnDate: LocalDate,
    val fee: BigDecimal?
)
