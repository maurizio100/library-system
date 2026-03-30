package com.library.catalog.domain

data class Author(val name: String) {
    init {
        require(name.isNotBlank()) { "Author is required" }
    }
}
