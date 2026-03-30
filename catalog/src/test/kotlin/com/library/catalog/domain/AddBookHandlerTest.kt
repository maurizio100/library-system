package com.library.catalog.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class AddBookHandlerTest {

    private val bookRepository = mockk<BookRepository>(relaxed = true)
    private val handler = AddBookHandler(bookRepository)

    private val command = AddBookCommand(
        isbn = ISBN("9780134685991"),
        title = "Effective Java",
        authors = listOf(Author("Joshua Bloch")),
        publicationYear = 2018
    )

    @Test
    fun `adds a new book and returns BookAdded event`() {
        every { bookRepository.existsByIsbn(command.isbn) } returns false

        val event = handler.handle(command)

        assertEquals("9780134685991", event.isbn)
        assertEquals("Effective Java", event.title)
        assertEquals(listOf("Joshua Bloch"), event.authors)
        assertEquals(2018, event.publicationYear)
        verify { bookRepository.save(any()) }
    }

    @Test
    fun `rejects duplicate ISBN`() {
        every { bookRepository.existsByIsbn(command.isbn) } returns true

        val ex = assertThrows<DuplicateIsbnException> { handler.handle(command) }
        assertEquals("ISBN already exists", ex.message)
        verify(exactly = 0) { bookRepository.save(any()) }
    }
}
