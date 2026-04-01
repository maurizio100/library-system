package com.library.catalog.bdd

import com.library.catalog.domain.port.ExternalBookCandidate
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MvcResult

/**
 * Shared mutable state for BDD scenarios that involve the "add book" form,
 * used by both the title-search and ISBN-lookup step definition classes.
 */
@Component
class TitleSearchState {
    var selectedIsbn: String? = null
    var selectedTitle: String? = null
    var selectedAuthors: String? = null
    var selectedYear: Int? = null
    var lastSearchResults: List<ExternalBookCandidate> = emptyList()
    var lastLookupResult: MvcResult? = null
    var lastErrorMessage: String? = null
    var clientSideValidationFailed: Boolean = false

    fun clear() {
        selectedIsbn = null
        selectedTitle = null
        selectedAuthors = null
        selectedYear = null
        lastSearchResults = emptyList()
        lastLookupResult = null
        lastErrorMessage = null
        clientSideValidationFailed = false
    }
}
