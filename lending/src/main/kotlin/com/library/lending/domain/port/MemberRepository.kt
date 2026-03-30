package com.library.lending.domain.port

import com.library.lending.domain.model.Member
import com.library.lending.domain.model.MemberId

interface MemberRepository {
    fun findById(memberId: MemberId): Member?
    fun save(member: Member)
}
