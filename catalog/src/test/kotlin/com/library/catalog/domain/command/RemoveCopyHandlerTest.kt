package com.library.catalog.domain.command

import com.library.catalog.domain.exception.CopyCurrentlyBorrowedException
import com.library.catalog.domain.exception.CopyNotFoundException
import com.library.catalog.domain.model.Author
import com.library.catalog.domain.model.Barcode
import com.library.catalog.domain.model.Book
import com.library.catalog.domain.model.CopyStatus
import com.library.catalog.domain.model.ISBN
import com.library.catalog.domain.port.BookRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RemoveCopyHandlerTest {

    private val bookRepository = mockk<BookRepository>(relaxed = true)
    private val handler = RemoveCopyHandler(bookRepository)

    private val isbn = ISBN("9780134685991")
    private val barcode = Barcode("EJ-001")

    private fun bookWithAvailableCopy(): Book {
        val book = Book(isbn, "Effective Java", listOf(Author("Joshua Bloch")), 2018)
        book.registerCopy(barcode)
        return book
    }

    private fun bookWithBorrowedCopy(): Book {
        val book = bookWithAvailableCopy()
        book.copies.first { it.barcode == barcode }.status = CopyStatus.Borrowed
        return book
    }

    @Test
    fun `removes an available copy and saves`() {
        val book = bookWithAvailableCopy()
        every { bookRepository.findByBarcode(barcode) } returns book

        handler.handle(RemoveCopyCommand("EJ-001"))

        assertEquals(0, book.copies.size)
        verify { bookRepository.save(book) }
    }

    @Test
    fun `rejects removal of a borrowed copy`() {
        val book = bookWithBorrowedCopy()
        every { bookRepository.findByBarcode(barcode) } returns book

        val ex = assertThrows<CopyCurrentlyBorrowedException> {
            handler.handle(RemoveCopyCommand("EJ-001"))
        }
        assertEquals("Cannot remove a copy that is currently borrowed", ex.message)
    }

    @Test
    fun `rejects removal when copy does not exist`() {
        every { bookRepository.findByBarcode(barcode) } returns null

        val ex = assertThrows<CopyNotFoundException> {
            handler.handle(RemoveCopyCommand("EJ-001"))
        }
        assertEquals("Copy not found", ex.message)
    }

    @Test
    fun `rejects blank barcode`() {
        val ex = assertThrows<IllegalArgumentException> {
            handler.handle(RemoveCopyCommand(""))
        }
        assertEquals("Barcode must not be blank", ex.message)
    }
}
