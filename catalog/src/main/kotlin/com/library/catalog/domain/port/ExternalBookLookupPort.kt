package com.library.catalog.domain.port

data class ExternalBookCandidate(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int?,
    val coverUrl: String? = null
)

interface ExternalBookLookupPort {
    fun searchByTitle(title: String): List<ExternalBookCandidate>
}
