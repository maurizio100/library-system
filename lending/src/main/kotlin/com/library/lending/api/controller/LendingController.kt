package com.library.lending.api.controller

import com.library.lending.api.dto.CreateLoanRequest
import com.library.lending.api.dto.LoanResponse
import com.library.lending.domain.command.CreateLoanCommand
import com.library.lending.domain.command.CreateLoanHandler
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
    private val eventPublisher: ApplicationEventPublisher
) {

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
}
