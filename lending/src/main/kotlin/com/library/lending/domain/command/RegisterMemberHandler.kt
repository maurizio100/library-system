package com.library.lending.domain.command

import com.library.lending.domain.event.MemberRegistered
import com.library.lending.domain.exception.DuplicateEmailException
import com.library.lending.domain.model.Member
import com.library.lending.domain.port.MemberRepository

class RegisterMemberHandler(
    private val memberRepository: MemberRepository
) {

    fun handle(command: RegisterMemberCommand): MemberRegistered {
        if (memberRepository.findByEmail(command.email) != null) {
            throw DuplicateEmailException(command.email)
        }

        val (member, event) = Member.register(
            name = command.name,
            email = command.email
        )

        memberRepository.save(member)
        return event
    }
}
