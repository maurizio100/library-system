package com.library.lending.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface LoanJpaRepository : JpaRepository<LoanJpaEntity, String> {
    fun findByCopyBarcodeAndStatus(copyBarcode: String, status: String): LoanJpaEntity?
    fun findFirstByCopyBarcodeOrderByLoanDateDesc(copyBarcode: String): LoanJpaEntity?
}
