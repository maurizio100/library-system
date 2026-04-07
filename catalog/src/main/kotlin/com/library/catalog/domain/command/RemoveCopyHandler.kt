package com.library.catalog.domain.command

import com.library.catalog.domain.exception.CopyNotFoundException
import com.library.catalog.domain.model.Barcode
import com.library.catalog.domain.port.BookRepository

class RemoveCopyHandler(private val bookRepository: BookRepository) {

    fun handle(command: RemoveCopyCommand) {
        require(command.barcode.isNotBlank()) { "Barcode must not be blank" }
        val barcode = Barcode(command.barcode)
        val book = bookRepository.findByBarcode(barcode) ?: throw CopyNotFoundException()
        book.removeCopy(barcode)
        bookRepository.save(book)
    }
}
