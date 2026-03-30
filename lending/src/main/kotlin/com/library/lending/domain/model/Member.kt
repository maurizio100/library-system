package com.library.lending.domain.model

import com.library.lending.domain.exception.BorrowingLimitReachedException

class Member(
    val memberId: MemberId,
    val name: String,
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
}
