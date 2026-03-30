package com.library.catalog.domain.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CopyTest {

    @Test
    fun `new copy has Available status`() {
        val copy = Copy(Barcode("EJ-001"))
        assertEquals(CopyStatus.Available, copy.status)
    }
}
