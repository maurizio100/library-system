package com.library.lending.domain.event

import com.library.shared.events.DomainEvent
import java.time.LocalDate

data class BookReturned(
    val loanId: String,
    val memberId: String,
    val copyBarcode: String,
    val returnDate: LocalDate
) : DomainEvent
