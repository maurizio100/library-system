package com.library.catalog.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class BookTest {

    @Test
    fun `create book returns book and BookAdded event`() {
        val (book, event) = Book.create(
            isbn = ISBN("9780134685991"),
            title = "Effective Java",
            authors = listOf(Author("Joshua Bloch")),
            publicationYear = 2018
        )

        assertEquals("9780134685991", book.isbn.value)
        assertEquals("Effective Java", book.title)
        assertEquals(1, book.authors.size)
        assertEquals("Joshua Bloch", book.authors[0].name)
        assertEquals(2018, book.publicationYear)

        assertEquals("9780134685991", event.isbn)
        assertEquals("Effective Java", event.title)
        assertEquals(listOf("Joshua Bloch"), event.authors)
        assertEquals(2018, event.publicationYear)
    }

    @Test
    fun `book with blank title is rejected`() {
        val ex = assertThrows<IllegalArgumentException> {
            Book.create(ISBN("9780134685991"), "", listOf(Author("Joshua Bloch")), 2018)
        }
        assertEquals("Title is required", ex.message)
    }

    @Test
    fun `book with empty authors is rejected`() {
        val ex = assertThrows<IllegalArgumentException> {
            Book.create(ISBN("9780134685991"), "Effective Java", emptyList(), 2018)
        }
        assertEquals("Author is required", ex.message)
    }
}
