package com.library.lending.domain.model

import com.library.lending.domain.event.MemberRegistered
import com.library.lending.domain.exception.BorrowingLimitReachedException
import com.library.lending.domain.exception.InvalidEmailException
import com.library.lending.domain.exception.MemberNameRequiredException
import java.util.UUID

class Member(
    val memberId: MemberId,
    val name: String,
    val email: String,
    val borrowingLimit: Int = 3,
    var activeLoansCount: Int = 0
) {
    fun checkCanBorrow() {
        if (activeLoansCount >= borrowingLimit) {
            throw BorrowingLimitReachedException(memberId)
        }
    }

    fun incrementActiveLoans() {
        activeLoansCount++
    }

    companion object {
        private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun register(name: String, email: String): Pair<Member, MemberRegistered> {
            if (name.isBlank()) {
                throw MemberNameRequiredException()
            }
            if (!EMAIL_PATTERN.matches(email)) {
                throw InvalidEmailException(email)
            }

            val memberId = MemberId(UUID.randomUUID().toString())
            val member = Member(
                memberId = memberId,
                name = name,
                email = email
            )
            val event = MemberRegistered(
                memberId = memberId.value,
                name = name,
                email = email
            )
            return Pair(member, event)
        }
    }
}
