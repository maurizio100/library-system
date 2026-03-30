package com.library.lending.domain.command

import com.library.lending.domain.exception.BorrowingLimitReachedException
import com.library.lending.domain.exception.CopyNotAvailableException
import com.library.lending.domain.exception.MemberNotFoundException
import com.library.lending.domain.model.Member
import com.library.lending.domain.model.MemberId
import com.library.lending.domain.port.CopyAvailabilityPort
import com.library.lending.domain.port.LoanRepository
import com.library.lending.domain.port.MemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CreateLoanHandlerTest {

    private val memberRepository = mockk<MemberRepository>(relaxed = true)
    private val loanRepository = mockk<LoanRepository>(relaxed = true)
    private val copyAvailabilityPort = mockk<CopyAvailabilityPort>(relaxed = true)
    private val handler = CreateLoanHandler(memberRepository, loanRepository, copyAvailabilityPort)

    private val memberId = MemberId("m-001")
    private val command = CreateLoanCommand(memberId, "EJ-001")

    @Test
    fun `creates loan for valid member and available copy`() {
        val member = Member(memberId, "Alice", borrowingLimit = 3, activeLoansCount = 0)
        every { memberRepository.findById(memberId) } returns member
        every { copyAvailabilityPort.isCopyAvailable("EJ-001") } returns true

        val event = handler.handle(command)

        assertEquals("m-001", event.memberId)
        assertEquals("EJ-001", event.copyBarcode)
        verify { loanRepository.save(any()) }
        verify { memberRepository.save(any()) }
        verify { copyAvailabilityPort.markCopyAsBorrowed("EJ-001") }
    }

    @Test
    fun `rejects loan for unknown member`() {
        every { memberRepository.findById(memberId) } returns null

        val ex = assertThrows<MemberNotFoundException> { handler.handle(command) }
        assertEquals("Member not found", ex.message)
    }

    @Test
    fun `rejects loan when borrowing limit reached`() {
        val member = Member(memberId, "Alice", borrowingLimit = 3, activeLoansCount = 3)
        every { memberRepository.findById(memberId) } returns member

        val ex = assertThrows<BorrowingLimitReachedException> { handler.handle(command) }
        assertEquals("Borrowing limit reached", ex.message)
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    @Test
    fun `rejects loan for unavailable copy`() {
        val member = Member(memberId, "Alice", borrowingLimit = 3, activeLoansCount = 0)
        every { memberRepository.findById(memberId) } returns member
        every { copyAvailabilityPort.isCopyAvailable("EJ-001") } returns false

        val ex = assertThrows<CopyNotAvailableException> { handler.handle(command) }
        assertEquals("Copy is not available", ex.message)
        verify(exactly = 0) { loanRepository.save(any()) }
    }
}
