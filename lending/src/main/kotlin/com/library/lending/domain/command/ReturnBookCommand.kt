package com.library.lending.domain.command

import com.library.lending.domain.model.MemberId

data class ReturnBookCommand(
    val memberId: MemberId,
    val copyBarcode: String
)
