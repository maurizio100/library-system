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
        assertEquals("ISBN must contain exactly 13 digits", ex.message)
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

    @Test
    fun `valid hyphenated ISBN is accepted`() {
        val isbn = assertDoesNotThrow { ISBN("978-3-16-148410-0") }
        assertEquals("9783161484100", isbn.value)
    }

    @Test
    fun `hyphenated ISBN is normalised to digits only`() {
        assertEquals("9780134685991", ISBN("978-0-13-468599-1").value)
    }

    @Test
    fun `hyphenated ISBN with fewer than 13 digits is rejected`() {
        val ex = assertThrows<IllegalArgumentException> { ISBN("978-3-16-14841-0") }
        assertEquals("ISBN must contain exactly 13 digits", ex.message)
    }

    @Test
    fun `hyphenated ISBN with more than 13 digits is rejected`() {
        assertThrows<IllegalArgumentException> { ISBN("978-3-16-1484100-0") }
    }

    @Test
    fun `ISBN with non-digit non-dash character is rejected`() {
        val ex = assertThrows<IllegalArgumentException> { ISBN("978-3-16-14841O-0") }
        assertEquals("ISBN must contain exactly 13 digits", ex.message)
    }
}
