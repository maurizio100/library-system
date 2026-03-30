package com.library.lending.domain.model

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

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
}
