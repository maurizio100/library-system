package com.library.catalog.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ISBNTest {

    @Test
    fun `valid 13-digit ISBN is accepted`() {
        val isbn = assertDoesNotThrow { ISBN("9780134685991") }
        assertEquals("9780134685991", isbn.value)
    }

    @Test
    fun `ISBN with fewer than 13 digits is rejected`() {
        val ex = assertThrows<IllegalArgumentException> { ISBN("123") }
        assertEquals("Invalid ISBN", ex.message)
    }

    @Test
    fun `ISBN with more than 13 digits is rejected`() {
        assertThrows<IllegalArgumentException> { ISBN("97801346859911") }
    }

    @Test
    fun `ISBN with non-numeric characters is rejected`() {
        assertThrows<IllegalArgumentException> { ISBN("978013468599X") }
    }

    @Test
    fun `empty ISBN is rejected`() {
        assertThrows<IllegalArgumentException> { ISBN("") }
    }
}
