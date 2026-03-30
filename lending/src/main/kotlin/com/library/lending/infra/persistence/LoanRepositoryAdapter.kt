package com.library.lending.infra.persistence

import com.library.lending.domain.model.Loan
import com.library.lending.domain.port.LoanRepository
import org.springframework.stereotype.Repository

@Repository
class LoanRepositoryAdapter(
    private val jpaRepository: LoanJpaRepository
) : LoanRepository {

    override fun save(loan: Loan) {
        jpaRepository.save(loan.toJpaEntity())
    }

    private fun Loan.toJpaEntity(): LoanJpaEntity {
        return LoanJpaEntity(
            loanId = loanId.value.toString(),
            memberId = memberId.value,
            copyBarcode = copyBarcode,
            loanDate = loanDate,
            dueDate = dueDate,
            status = status.name
        )
    }
}
