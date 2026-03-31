package com.library.lending.infra.persistence

import com.library.lending.domain.model.Member
import com.library.lending.domain.model.MemberId
import com.library.lending.domain.port.MemberRepository
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryAdapter(
    private val jpaRepository: MemberJpaRepository
) : MemberRepository {

    override fun findById(memberId: MemberId): Member? {
        return jpaRepository.findById(memberId.value).orElse(null)?.toDomain()
    }

    override fun findByEmail(email: String): Member? {
        return jpaRepository.findByEmail(email)?.toDomain()
    }

    override fun findAll(): List<Member> {
        return jpaRepository.findAll().map { it.toDomain() }
    }

    override fun save(member: Member) {
        jpaRepository.save(member.toJpaEntity())
    }

    private fun MemberJpaEntity.toDomain(): Member {
        return Member(
            memberId = MemberId(memberId),
            name = name,
            email = email,
            borrowingLimit = borrowingLimit,
            activeLoansCount = activeLoansCount
        )
    }

    private fun Member.toJpaEntity(): MemberJpaEntity {
        return MemberJpaEntity(
            memberId = memberId.value,
            name = name,
            email = email,
            borrowingLimit = borrowingLimit,
            activeLoansCount = activeLoansCount
        )
    }
}
