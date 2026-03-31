package com.library.lending.api.controller

import com.library.lending.api.dto.CreateLoanRequest
import com.library.lending.api.dto.LoanResponse
import com.library.lending.api.dto.MemberResponse
import com.library.lending.api.dto.RegisterMemberRequest
import com.library.lending.api.dto.ReturnBookRequest
import com.library.lending.api.dto.ReturnResponse
import com.library.lending.domain.command.CreateLoanCommand
import com.library.lending.domain.command.CreateLoanHandler
import com.library.lending.domain.command.RegisterMemberCommand
import com.library.lending.domain.command.RegisterMemberHandler
import com.library.lending.domain.command.ReturnBookCommand
import com.library.lending.domain.command.ReturnBookHandler
import com.library.lending.domain.model.MemberId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/lending")
class LendingController(
    private val createLoanHandler: CreateLoanHandler,
    private val registerMemberHandler: RegisterMemberHandler,
    private val returnBookHandler: ReturnBookHandler,
    private val eventPublisher: ApplicationEventPublisher
) {

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerMember(@RequestBody request: RegisterMemberRequest): MemberResponse {
        val command = RegisterMemberCommand(
            name = request.name,
            email = request.email
        )
        val event = registerMemberHandler.handle(command)
        eventPublisher.publishEvent(event)
        return MemberResponse(
            memberId = event.memberId,
            name = event.name,
            email = event.email,
            borrowingLimit = 3
        )
    }

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    fun createLoan(@RequestBody request: CreateLoanRequest): LoanResponse {
        val command = CreateLoanCommand(
            memberId = MemberId(request.memberId),
            copyBarcode = request.copyBarcode
        )
        val event = createLoanHandler.handle(command)
        eventPublisher.publishEvent(event)
        return LoanResponse(
            loanId = event.loanId,
            memberId = event.memberId,
            copyBarcode = event.copyBarcode,
            loanDate = event.loanDate,
            dueDate = event.dueDate,
            status = "Active"
        )
    }

    @PostMapping("/returns")
    @ResponseStatus(HttpStatus.OK)
    fun returnBook(@RequestBody request: ReturnBookRequest): ReturnResponse {
        val command = ReturnBookCommand(
            memberId = MemberId(request.memberId),
            copyBarcode = request.copyBarcode
        )
        val result = returnBookHandler.handle(command)
        result.events.forEach { eventPublisher.publishEvent(it) }

        val bookReturnedEvent = result.events
            .filterIsInstance<com.library.lending.domain.event.BookReturned>()
            .first()

        return ReturnResponse(
            loanId = bookReturnedEvent.loanId,
            returnDate = bookReturnedEvent.returnDate,
            fee = result.fee?.amount
        )
    }
}
