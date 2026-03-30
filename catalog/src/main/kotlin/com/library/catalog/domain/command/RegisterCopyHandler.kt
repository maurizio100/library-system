package com.library.catalog.domain.command

import com.library.catalog.domain.event.CopyRegistered
import com.library.catalog.domain.exception.BookNotFoundException
import com.library.catalog.domain.port.BookRepository

class RegisterCopyHandler(private val bookRepository: BookRepository) {

    fun handle(command: RegisterCopyCommand): CopyRegistered {
        val book = bookRepository.findByIsbn(command.isbn)
            ?: throw BookNotFoundException(command.isbn)
        val event = book.registerCopy(command.barcode)
        bookRepository.save(book)
        return event
    }
}
