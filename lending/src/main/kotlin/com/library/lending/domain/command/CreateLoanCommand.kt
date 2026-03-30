package com.library.lending.domain.command

import com.library.lending.domain.model.MemberId

data class CreateLoanCommand(
    val memberId: MemberId,
    val copyBarcode: String
)
