package com.library.lending.domain.exception

import com.library.lending.domain.model.MemberId

class MemberNotFoundException(memberId: MemberId) : RuntimeException("Member not found")
