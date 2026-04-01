package com.library.catalog.infra.external

import com.library.catalog.domain.exception.ExternalLookupUnavailableException
import com.library.catalog.domain.port.ExternalBookCandidate
import com.library.catalog.domain.port.ExternalBookLookupPort
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class OpenLibraryAdapter(restClientBuilder: RestClient.Builder) : ExternalBookLookupPort {

    private val restClient = restClientBuilder
        .baseUrl("https://openlibrary.org")
        .build()

    override fun searchByTitle(title: String): List<ExternalBookCandidate> {
        val response = try {
            restClient.get()
                .uri("/search.json?title={title}&fields=isbn,title,author_name,first_publish_year&limit=10", title)
                .retrieve()
                .body(OpenLibrarySearchResponse::class.java)
        } catch (ex: RestClientException) {
            throw ExternalLookupUnavailableException(
                "Book search service is currently unavailable — please try again later"
            )
        }

        return response?.docs
            ?.mapNotNull { doc ->
                val isbn = doc.isbn?.firstOrNull { it.length == 13 } ?: return@mapNotNull null
                ExternalBookCandidate(
                    isbn = isbn,
                    title = doc.title ?: return@mapNotNull null,
                    authors = doc.authorName ?: emptyList(),
                    publicationYear = doc.firstPublishYear
                )
            }
            ?: emptyList()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OpenLibrarySearchResponse(val docs: List<OpenLibraryDoc>?)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OpenLibraryDoc(
        val isbn: List<String>?,
        val title: String?,
        @JsonProperty("author_name") val authorName: List<String>?,
        @JsonProperty("first_publish_year") val firstPublishYear: Int?
    )
}
