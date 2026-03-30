package com.library.catalog.domain.port

data class BookSearchResult(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int,
    val totalCopies: Int,
    val availableCopies: Int
)

interface BookSearchPort {
    fun search(query: String): List<BookSearchResult>
    fun findAll(): List<BookSearchResult>
}
