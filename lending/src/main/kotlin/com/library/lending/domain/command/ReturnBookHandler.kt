package com.library.lending.domain.command

import com.library.lending.domain.exception.NoActiveLoanFoundException
import com.library.lending.domain.model.Loan
import com.library.lending.domain.port.CopyAvailabilityPort
import com.library.lending.domain.port.LoanRepository
import com.library.lending.domain.port.MemberRepository
import java.time.LocalDate

class ReturnBookHandler(
    private val loanRepository: LoanRepository,
    private val memberRepository: MemberRepository,
    private val copyAvailabilityPort: CopyAvailabilityPort
) {

    fun handle(command: ReturnBookCommand, today: LocalDate = LocalDate.now()): Loan.ReturnResult {
        val loan = loanRepository.findActiveLoanByCopyBarcode(command.copyBarcode)

        if (loan == null) {
            val latestLoan = loanRepository.findLatestLoanByCopyBarcode(command.copyBarcode)
            if (latestLoan != null) {
                // This will throw LoanAlreadyReturnedException
                latestLoan.returnBook(today)
            }
            throw NoActiveLoanFoundException(command.copyBarcode)
        }

        val result = loan.returnBook(today)

        loanRepository.save(loan)

        val member = memberRepository.findById(loan.memberId)
        if (member != null) {
            member.decrementActiveLoans()
            memberRepository.save(member)
        }

        copyAvailabilityPort.markCopyAsAvailable(loan.copyBarcode)

        return result
    }
}
