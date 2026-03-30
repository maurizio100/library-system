package com.library.lending.domain.model

import com.library.lending.domain.exception.BorrowingLimitReachedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MemberTest {

    @Test
    fun `member with loans below limit can borrow`() {
        val member = Member(MemberId("m-001"), "Alice", borrowingLimit = 3, activeLoansCount = 2)
        assertDoesNotThrow { member.checkCanBorrow() }
    }

    @Test
    fun `member at borrowing limit cannot borrow`() {
        val member = Member(MemberId("m-001"), "Alice", borrowingLimit = 3, activeLoansCount = 3)
        val ex = assertThrows<BorrowingLimitReachedException> { member.checkCanBorrow() }
        assertEquals("Borrowing limit reached", ex.message)
    }

    @Test
    fun `incrementActiveLoans increases count`() {
        val member = Member(MemberId("m-001"), "Alice", activeLoansCount = 1)
        member.incrementActiveLoans()
        assertEquals(2, member.activeLoansCount)
    }
}
