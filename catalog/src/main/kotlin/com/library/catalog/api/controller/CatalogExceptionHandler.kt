package com.library.catalog.api.controller

import com.library.catalog.api.dto.ErrorResponse
import com.library.catalog.domain.exception.BookNotFoundException
import com.library.catalog.domain.exception.CopyCurrentlyBorrowedException
import com.library.catalog.domain.exception.CopyNotFoundException
import com.library.catalog.domain.exception.DuplicateBarcodeException
import com.library.catalog.domain.exception.DuplicateIsbnException
import com.library.catalog.domain.exception.ExternalLookupUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["com.library.catalog"])
class CatalogExceptionHandler {

    @ExceptionHandler(DuplicateIsbnException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateIsbn(ex: DuplicateIsbnException): ErrorResponse {
        return ErrorResponse(ex.message ?: "ISBN already exists")
    }

    @ExceptionHandler(DuplicateBarcodeException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateBarcode(ex: DuplicateBarcodeException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Barcode already exists")
    }

    @ExceptionHandler(BookNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleBookNotFound(ex: BookNotFoundException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Book not found")
    }

    @ExceptionHandler(CopyNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCopyNotFound(ex: CopyNotFoundException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Copy not found")
    }

    @ExceptionHandler(CopyCurrentlyBorrowedException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleCopyCurrentlyBorrowed(ex: CopyCurrentlyBorrowedException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Cannot remove a copy that is currently borrowed")
    }

    @ExceptionHandler(ExternalLookupUnavailableException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleExternalLookupUnavailable(ex: ExternalLookupUnavailableException): ErrorResponse {
        return ErrorResponse(ex.message ?: "External lookup service unavailable")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(ex.message ?: "Invalid request")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): ErrorResponse {
        val message = ex.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage ?: "Validation failed"
        return ErrorResponse(message)
    }
}
