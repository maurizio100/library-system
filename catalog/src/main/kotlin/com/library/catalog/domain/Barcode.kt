package com.library.catalog.domain

data class Barcode(val value: String) {
    init {
        require(value.isNotBlank()) { "Barcode is required" }
    }
}
