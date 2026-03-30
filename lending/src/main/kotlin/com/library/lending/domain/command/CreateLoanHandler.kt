package com.library.lending.domain.command

import com.library.lending.domain.event.LoanCreated
import com.library.lending.domain.exception.CopyNotAvailableException
import com.library.lending.domain.exception.MemberNotFoundException
import com.library.lending.domain.model.Loan
import com.library.lending.domain.port.CopyAvailabilityPort
import com.library.lending.domain.port.LoanRepository
import com.library.lending.domain.port.MemberRepository

class CreateLoanHandler(
    private val memberRepository: MemberRepository,
    private val loanRepository: LoanRepository,
    private val copyAvailabilityPort: CopyAvailabilityPort
) {

    fun handle(command: CreateLoanCommand): LoanCreated {
        val member = memberRepository.findById(command.memberId)
            ?: throw MemberNotFoundException(command.memberId)

        member.checkCanBorrow()

        if (!copyAvailabilityPort.isCopyAvailable(command.copyBarcode)) {
            throw CopyNotAvailableException(command.copyBarcode)
        }

        val (loan, event) = Loan.create(
            memberId = command.memberId,
            copyBarcode = command.copyBarcode
        )

        loanRepository.save(loan)
        member.incrementActiveLoans()
        memberRepository.save(member)
        copyAvailabilityPort.markCopyAsBorrowed(command.copyBarcode)

        return event
    }
}
