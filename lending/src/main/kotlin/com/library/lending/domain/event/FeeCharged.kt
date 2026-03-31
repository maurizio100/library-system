package com.library.lending.domain.event

import com.library.shared.events.DomainEvent
import java.math.BigDecimal

data class FeeCharged(
    val loanId: String,
    val memberId: String,
    val amount: BigDecimal,
    val daysOverdue: Long
) : DomainEvent
