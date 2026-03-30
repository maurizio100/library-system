package com.library.lending.domain.model

import com.library.lending.domain.event.LoanCreated
import java.time.LocalDate

class Loan(
    val loanId: LoanId,
    val memberId: MemberId,
    val copyBarcode: String,
    val loanDate: LocalDate,
    val dueDate: LocalDate,
    val status: LoanStatus = LoanStatus.Active
) {
    companion object {
        private const val LOAN_PERIOD_DAYS = 14L

        fun create(memberId: MemberId, copyBarcode: String, loanDate: LocalDate = LocalDate.now()): Pair<Loan, LoanCreated> {
            val loanId = LoanId()
            val dueDate = loanDate.plusDays(LOAN_PERIOD_DAYS)
            val loan = Loan(
                loanId = loanId,
                memberId = memberId,
                copyBarcode = copyBarcode,
                loanDate = loanDate,
                dueDate = dueDate
            )
            val event = LoanCreated(
                loanId = loanId.value.toString(),
                memberId = memberId.value,
                copyBarcode = copyBarcode,
                loanDate = loanDate,
                dueDate = dueDate
            )
            return loan to event
        }
    }
}
