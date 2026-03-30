package com.library.catalog.domain

data class ISBN(val value: String) {
    init {
        require(value.matches(Regex("^\\d{13}$"))) { "Invalid ISBN" }
    }
}
