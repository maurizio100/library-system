package com.library.lending.infra.config

import com.library.lending.domain.command.CreateLoanHandler
import com.library.lending.domain.command.RegisterMemberHandler
import com.library.lending.domain.command.ReturnBookHandler
import com.library.lending.domain.port.CopyAvailabilityPort
import com.library.lending.domain.port.LoanRepository
import com.library.lending.domain.port.MemberRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LendingConfiguration {

    @Bean
    fun createLoanHandler(
        memberRepository: MemberRepository,
        loanRepository: LoanRepository,
        copyAvailabilityPort: CopyAvailabilityPort
    ): CreateLoanHandler {
        return CreateLoanHandler(memberRepository, loanRepository, copyAvailabilityPort)
    }

    @Bean
    fun registerMemberHandler(
        memberRepository: MemberRepository
    ): RegisterMemberHandler {
        return RegisterMemberHandler(memberRepository)
    }

    @Bean
    fun returnBookHandler(
        loanRepository: LoanRepository,
        memberRepository: MemberRepository,
        copyAvailabilityPort: CopyAvailabilityPort
    ): ReturnBookHandler {
        return ReturnBookHandler(loanRepository, memberRepository, copyAvailabilityPort)
    }
}
