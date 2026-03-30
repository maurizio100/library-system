package com.library.lending.domain.event

import com.library.shared.events.DomainEvent
import java.time.LocalDate

data class LoanCreated(
    val loanId: String,
    val memberId: String,
    val copyBarcode: String,
    val loanDate: LocalDate,
    val dueDate: LocalDate
) : DomainEvent
