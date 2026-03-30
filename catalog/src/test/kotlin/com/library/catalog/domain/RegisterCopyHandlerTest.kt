package com.library.catalog.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RegisterCopyHandlerTest {

    private val bookRepository = mockk<BookRepository>(relaxed = true)
    private val handler = RegisterCopyHandler(bookRepository)

    private val isbn = ISBN("9780134685991")
    private val book = Book(isbn, "Effective Java", listOf(Author("Joshua Bloch")), 2018)

    @Test
    fun `registers a copy and returns CopyRegistered event`() {
        every { bookRepository.findByIsbn(isbn) } returns book

        val event = handler.handle(RegisterCopyCommand(isbn, Barcode("EJ-001")))

        assertEquals("9780134685991", event.isbn)
        assertEquals("EJ-001", event.barcode)
        verify { bookRepository.save(book) }
    }

    @Test
    fun `rejects copy for nonexistent book`() {
        every { bookRepository.findByIsbn(isbn) } returns null

        val ex = assertThrows<BookNotFoundException> {
            handler.handle(RegisterCopyCommand(isbn, Barcode("EJ-001")))
        }
        assertEquals("Book not found", ex.message)
    }

    @Test
    fun `rejects duplicate barcode`() {
        book.registerCopy(Barcode("EJ-001"))
        every { bookRepository.findByIsbn(isbn) } returns book

        val ex = assertThrows<DuplicateBarcodeException> {
            handler.handle(RegisterCopyCommand(isbn, Barcode("EJ-001")))
        }
        assertEquals("Barcode already exists", ex.message)
    }
}
