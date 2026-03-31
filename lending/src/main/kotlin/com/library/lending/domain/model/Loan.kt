package com.library.lending.domain.model

import com.library.lending.domain.event.BookReturned
import com.library.lending.domain.event.FeeCharged
import com.library.lending.domain.event.LoanCreated
import com.library.lending.domain.exception.LoanAlreadyReturnedException
import com.library.shared.events.DomainEvent
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Loan(
    val loanId: LoanId,
    val memberId: MemberId,
    val copyBarcode: String,
    val loanDate: LocalDate,
    val dueDate: LocalDate,
    var status: LoanStatus = LoanStatus.Active,
    var returnDate: LocalDate? = null
) {

    fun returnBook(today: LocalDate = LocalDate.now()): ReturnResult {
        if (status == LoanStatus.Returned) {
            throw LoanAlreadyReturnedException()
        }

        val events = mutableListOf<DomainEvent>()
        var fee: Fee? = null

        val daysOverdue = ChronoUnit.DAYS.between(dueDate, today)
        if (daysOverdue > 0) {
            fee = Fee.calculate(daysOverdue)
            events.add(
                FeeCharged(
                    loanId = loanId.value.toString(),
                    memberId = memberId.value,
                    amount = fee.amount,
                    daysOverdue = daysOverdue
                )
            )
        }

        events.add(
            BookReturned(
                loanId = loanId.value.toString(),
                memberId = memberId.value,
                copyBarcode = copyBarcode,
                returnDate = today
            )
        )

        status = LoanStatus.Returned
        returnDate = today

        return ReturnResult(events = events, fee = fee)
    }

    data class ReturnResult(
        val events: List<DomainEvent>,
        val fee: Fee?
    )

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
