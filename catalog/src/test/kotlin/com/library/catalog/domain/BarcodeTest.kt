package com.library.catalog.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class BarcodeTest {

    @Test
    fun `valid barcode is accepted`() {
        val barcode = assertDoesNotThrow { Barcode("EJ-001") }
        assertEquals("EJ-001", barcode.value)
    }

    @Test
    fun `blank barcode is rejected`() {
        val ex = assertThrows<IllegalArgumentException> { Barcode("") }
        assertEquals("Barcode is required", ex.message)
    }

    @Test
    fun `whitespace-only barcode is rejected`() {
        assertThrows<IllegalArgumentException> { Barcode("   ") }
    }
}
