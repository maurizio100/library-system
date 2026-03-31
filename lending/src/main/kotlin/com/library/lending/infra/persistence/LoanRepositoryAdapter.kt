package com.library.lending.infra.persistence

import com.library.lending.domain.model.Loan
import com.library.lending.domain.model.LoanId
import com.library.lending.domain.model.LoanStatus
import com.library.lending.domain.model.MemberId
import com.library.lending.domain.port.LoanRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LoanRepositoryAdapter(
    private val jpaRepository: LoanJpaRepository
) : LoanRepository {

    override fun save(loan: Loan) {
        jpaRepository.save(loan.toJpaEntity())
    }

    override fun findActiveLoanByCopyBarcode(copyBarcode: String): Loan? {
        val entity = jpaRepository.findByCopyBarcodeAndStatus(copyBarcode, "Active")
            ?: jpaRepository.findByCopyBarcodeAndStatus(copyBarcode, "Overdue")
        return entity?.toDomain()
    }

    override fun findLatestLoanByCopyBarcode(copyBarcode: String): Loan? {
        return jpaRepository.findFirstByCopyBarcodeOrderByLoanDateDesc(copyBarcode)?.toDomain()
    }

    private fun Loan.toJpaEntity(): LoanJpaEntity {
        return LoanJpaEntity(
            loanId = loanId.value.toString(),
            memberId = memberId.value,
            copyBarcode = copyBarcode,
            loanDate = loanDate,
            dueDate = dueDate,
            status = status.name,
            returnDate = returnDate
        )
    }

    private fun LoanJpaEntity.toDomain(): Loan {
        return Loan(
            loanId = LoanId(UUID.fromString(loanId)),
            memberId = MemberId(memberId),
            copyBarcode = copyBarcode,
            loanDate = loanDate,
            dueDate = dueDate,
            status = LoanStatus.valueOf(status),
            returnDate = returnDate
        )
    }
}
