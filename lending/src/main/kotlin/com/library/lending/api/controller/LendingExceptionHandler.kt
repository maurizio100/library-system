package com.library.lending.api.controller

import com.library.lending.api.dto.ErrorResponse
import com.library.lending.domain.exception.BorrowingLimitReachedException
import com.library.lending.domain.exception.CopyNotAvailableException
import com.library.lending.domain.exception.DuplicateEmailException
import com.library.lending.domain.exception.InvalidEmailException
import com.library.lending.domain.exception.MemberNameRequiredException
import com.library.lending.domain.exception.MemberNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["com.library.lending"])
class LendingExceptionHandler {

    @ExceptionHandler(MemberNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleMemberNotFound(ex: MemberNotFoundException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Member not found")
    }

    @ExceptionHandler(BorrowingLimitReachedException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleBorrowingLimitReached(ex: BorrowingLimitReachedException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Borrowing limit reached")
    }

    @ExceptionHandler(CopyNotAvailableException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleCopyNotAvailable(ex: CopyNotAvailableException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Copy is not available")
    }

    @ExceptionHandler(DuplicateEmailException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ErrorResponse {
        return ErrorResponse(ex.message ?: "A member with this email already exists")
    }

    @ExceptionHandler(InvalidEmailException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidEmail(ex: InvalidEmailException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Invalid email address")
    }

    @ExceptionHandler(MemberNameRequiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMemberNameRequired(ex: MemberNameRequiredException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Member name is required")
    }
}
