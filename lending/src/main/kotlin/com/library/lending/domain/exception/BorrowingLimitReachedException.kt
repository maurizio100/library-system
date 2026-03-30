package com.library.lending.domain.exception

import com.library.lending.domain.model.MemberId

class BorrowingLimitReachedException(memberId: MemberId) : RuntimeException("Borrowing limit reached")
