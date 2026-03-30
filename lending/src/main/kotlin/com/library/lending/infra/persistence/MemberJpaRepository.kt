package com.library.lending.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberJpaEntity, String> {
    fun findByEmail(email: String): MemberJpaEntity?
}
