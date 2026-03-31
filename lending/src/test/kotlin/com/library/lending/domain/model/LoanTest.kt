package com.library.lending.domain.model

import com.library.lending.domain.event.BookReturned
import com.library.lending.domain.event.FeeCharged
import com.library.lending.domain.exception.LoanAlreadyReturnedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LoanTest {

    @Test
    fun `create loan sets due date 14 days from loan date`() {
        val loanDate = LocalDate.of(2026, 3, 30)
        val (loan, event) = Loan.create(MemberId("m-001"), "EJ-001", loanDate)

        assertEquals("m-001", loan.memberId.value)
        assertEquals("EJ-001", loan.copyBarcode)
        assertEquals(loanDate, loan.loanDate)
        assertEquals(LocalDate.of(2026, 4, 13), loan.dueDate)
        assertEquals(LoanStatus.Active, loan.status)

        assertEquals("m-001", event.memberId)
        assertEquals("EJ-001", event.copyBarcode)
        assertEquals(loanDate, event.loanDate)
        assertEquals(LocalDate.of(2026, 4, 13), event.dueDate)
    }

    @Test
    fun `return on time sets status to Returned with no fee`() {
        val loanDate = LocalDate.of(2026, 3, 17)
        val (loan, _) = Loan.create(MemberId("m-001"), "EJ-001", loanDate)

        val returnDate = LocalDate.of(2026, 3, 24) // 7 days, within 14-day period
        val result = loan.returnBook(returnDate)

        assertEquals(LoanStatus.Returned, loan.status)
        assertEquals(returnDate, loan.returnDate)
        assertNull(result.fee)

        val bookReturnedEvents = result.events.filterIsInstance<BookReturned>()
        assertEquals(1, bookReturnedEvents.size)
        assertEquals("m-001", bookReturnedEvents[0].memberId)
        assertEquals("EJ-001", bookReturnedEvents[0].copyBarcode)
        assertEquals(returnDate, bookReturnedEvents[0].returnDate)

        val feeEvents = result.events.filterIsInstance<FeeCharged>()
        assertEquals(0, feeEvents.size)
    }

    @Test
    fun `return on due date incurs no fee`() {
        val loanDate = LocalDate.of(2026, 3, 17)
        val (loan, _) = Loan.create(MemberId("m-001"), "EJ-001", loanDate)

        val returnDate = LocalDate.of(2026, 3, 31) // exactly 14 days
        val result = loan.returnBook(returnDate)

        assertEquals(LoanStatus.Returned, loan.status)
        assertNull(result.fee)
    }

    @Test
    fun `return overdue book incurs fee`() {
        val loanDate = LocalDate.of(2026, 3, 12)
        val (loan, _) = Loan.create(MemberId("m-001"), "EJ-001", loanDate)
        // due date = 2026-03-26

        val returnDate = LocalDate.of(2026, 3, 31) // 5 days overdue
        val result = loan.returnBook(returnDate)

        assertEquals(LoanStatus.Returned, loan.status)
        assertNotNull(result.fee)
        assertEquals(BigDecimal("2.50"), result.fee!!.amount)
        assertEquals(5L, result.fee!!.daysOverdue)

        val feeEvents = result.events.filterIsInstance<FeeCharged>()
        assertEquals(1, feeEvents.size)
        assertEquals(BigDecimal("2.50"), feeEvents[0].amount)
        assertEquals(5L, feeEvents[0].daysOverdue)
    }

    @Test
    fun `return already returned loan throws exception`() {
        val loanDate = LocalDate.of(2026, 3, 17)
        val (loan, _) = Loan.create(MemberId("m-001"), "EJ-001", loanDate)
        loan.returnBook(LocalDate.of(2026, 3, 24))

        assertThrows<LoanAlreadyReturnedException> {
            loan.returnBook(LocalDate.of(2026, 3, 25))
        }
    }
}
