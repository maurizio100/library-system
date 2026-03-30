package com.library.catalog.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class AuthorTest {

    @Test
    fun `valid author name is accepted`() {
        val author = assertDoesNotThrow { Author("Joshua Bloch") }
        assertEquals("Joshua Bloch", author.name)
    }

    @Test
    fun `blank author name is rejected`() {
        val ex = assertThrows<IllegalArgumentException> { Author("") }
        assertEquals("Author is required", ex.message)
    }

    @Test
    fun `whitespace-only author name is rejected`() {
        assertThrows<IllegalArgumentException> { Author("   ") }
    }
}
