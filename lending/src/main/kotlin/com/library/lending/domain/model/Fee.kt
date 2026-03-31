package com.library.lending.domain.model

import java.math.BigDecimal

data class Fee(
    val amount: BigDecimal,
    val daysOverdue: Long
) {
    companion object {
        private val DAILY_RATE = BigDecimal("0.50")

        fun calculate(daysOverdue: Long): Fee {
            return Fee(
                amount = DAILY_RATE.multiply(BigDecimal(daysOverdue)),
                daysOverdue = daysOverdue
            )
        }
    }
}
