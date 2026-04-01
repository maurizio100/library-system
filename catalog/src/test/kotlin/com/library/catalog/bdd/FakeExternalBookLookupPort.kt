package com.library.catalog.bdd

import com.library.catalog.domain.exception.ExternalLookupUnavailableException
import com.library.catalog.domain.port.ExternalBookCandidate
import com.library.catalog.domain.port.ExternalBookLookupPort
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class FakeExternalBookLookupPort : ExternalBookLookupPort {

    private var titleToResults: Map<String, List<ExternalBookCandidate>> = emptyMap()
    private var unavailable: Boolean = false
    var callCount: Int = 0

    fun configure(titleToResults: Map<String, List<ExternalBookCandidate>>) {
        this.titleToResults = titleToResults
        this.unavailable = false
    }

    fun setUnavailable() {
        this.unavailable = true
    }

    fun reset() {
        titleToResults = emptyMap()
        unavailable = false
        callCount = 0
    }

    override fun searchByTitle(title: String): List<ExternalBookCandidate> {
        callCount += 1
        if (unavailable) {
            throw ExternalLookupUnavailableException(
                "Book search service is currently unavailable — please try again later"
            )
        }
        return titleToResults[title] ?: emptyList()
    }
}
