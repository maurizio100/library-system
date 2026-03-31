package com.library.lending.domain.port

import com.library.lending.domain.model.Loan

interface LoanRepository {
    fun save(loan: Loan)
    fun findActiveLoanByCopyBarcode(copyBarcode: String): Loan?
    fun findLatestLoanByCopyBarcode(copyBarcode: String): Loan?
}
