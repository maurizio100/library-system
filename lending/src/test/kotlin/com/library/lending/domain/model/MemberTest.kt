package com.library.lending.domain.model

import com.library.lending.domain.exception.BorrowingLimitReachedException
import com.library.lending.domain.exception.InvalidEmailException
import com.library.lending.domain.exception.MemberNameRequiredException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MemberTest {

    @Test
    fun `member with loans below limit can borrow`() {
        val member = Member(MemberId("m-001"), "Alice", "alice@example.com", borrowingLimit = 3, activeLoansCount = 2)
        assertDoesNotThrow { member.checkCanBorrow() }
    }

    @Test
    fun `member at borrowing limit cannot borrow`() {
        val member = Member(MemberId("m-001"), "Alice", "alice@example.com", borrowingLimit = 3, activeLoansCount = 3)
        val ex = assertThrows<BorrowingLimitReachedException> { member.checkCanBorrow() }
        assertEquals("Borrowing limit reached", ex.message)
    }

    @Test
    fun `incrementActiveLoans increases count`() {
        val member = Member(MemberId("m-001"), "Alice", "alice@example.com", activeLoansCount = 1)
        member.incrementActiveLoans()
        assertEquals(2, member.activeLoansCount)
    }

    @Test
    fun `decrementActiveLoans decreases count`() {
        val member = Member(MemberId("m-001"), "Alice", "alice@example.com", activeLoansCount = 2)
        member.decrementActiveLoans()
        assertEquals(1, member.activeLoansCount)
    }

    @Test
    fun `decrementActiveLoans does not go below zero`() {
        val member = Member(MemberId("m-001"), "Alice", "alice@example.com", activeLoansCount = 0)
        member.decrementActiveLoans()
        assertEquals(0, member.activeLoansCount)
    }

    @Test
    fun `register creates member with valid data`() {
        val (member, event) = Member.register("Alice Thompson", "alice@example.com")

        assertNotNull(member.memberId.value)
        assertEquals("Alice Thompson", member.name)
        assertEquals("alice@example.com", member.email)
        assertEquals(3, member.borrowingLimit)
        assertEquals(0, member.activeLoansCount)

        assertEquals(member.memberId.value, event.memberId)
        assertEquals("Alice Thompson", event.name)
        assertEquals("alice@example.com", event.email)
    }

    @Test
    fun `register rejects empty name`() {
        assertThrows<MemberNameRequiredException> {
            Member.register("", "alice@example.com")
        }
    }

    @Test
    fun `register rejects invalid email`() {
        assertThrows<InvalidEmailException> {
            Member.register("Alice", "not-an-email")
        }
    }
}
