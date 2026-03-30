package com.library.lending.domain.port

import com.library.lending.domain.model.Loan

interface LoanRepository {
    fun save(loan: Loan)
}
