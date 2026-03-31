package com.library.lending.infra.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "loans")
class LoanJpaEntity(
    @Id
    val loanId: String = "",
    val memberId: String = "",
    val copyBarcode: String = "",
    val loanDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate = LocalDate.now(),
    val status: String = "Active",
    val returnDate: LocalDate? = null
)
