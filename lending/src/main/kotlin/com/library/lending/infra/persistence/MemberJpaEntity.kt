package com.library.lending.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class MemberJpaEntity(
    @Id
    val memberId: String = "",
    val name: String = "",
    @Column(unique = true)
    val email: String = "",
    val borrowingLimit: Int = 3,
    val activeLoansCount: Int = 0
)
