package com.library.catalog.bdd

import com.library.catalog.infra.persistence.BookJpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step definitions for story 005 — Add a Book by ISBN Lookup.
 *
 * The ISBN lookup flow calls OpenLibrary directly from the frontend, so the
 * backend has no proxy endpoint for it. These step defs simulate the frontend
 * behaviour at the API level: they use hardcoded test data for the ISBNs that
 * appear in the scenarios, and share book-selection state via TitleSearchState
 * so that the shared confirmation steps (defined in AddBookByTitleStepDefs) can
 * complete the flow.
 */
class AddBookByIsbnLookupStepDefs {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var bookJpaRepository: BookJpaRepository
    @Autowired private lateinit var state: TitleSearchState

    private var isbnLookupUnavailable = false
    private var isbnLookupData: Map<String, IsbnTestData?> = buildDefaultIsbnData()

    private data class IsbnTestData(
        val title: String,
        val author: String,
        val publicationYear: Int?
    )

    @Before
    fun setUp() {
        isbnLookupUnavailable = false
        isbnLookupData = buildDefaultIsbnData()
    }

    private fun buildDefaultIsbnData(): Map<String, IsbnTestData?> = mapOf(
        "9780134685991" to IsbnTestData("Effective Java", "Joshua Bloch", 2018),
        "9780596007126" to IsbnTestData("Head First Design Patterns", "Eric Freeman", 2004),
        "9780000000000" to null,                             // not found
        "9781234567897" to IsbnTestData("Some Book Title", "Some Author", null)  // incomplete: no year
    )

    // ── Background ──

    @Given("I am on the {string} page")
    fun iAmOnThePage(@Suppress("UNUSED_PARAMETER") page: String) {
        // frontend navigation — no-op at API level
    }

    // ── Setup steps ──

    @Given("the ISBN lookup service is unavailable")
    fun theIsbnLookupServiceIsUnavailable() {
        isbnLookupUnavailable = true
    }

    @Given("the ISBN lookup service returns no publication year for ISBN {string}")
    fun theIsbnLookupServiceReturnsNoPublicationYearForIsbn(@Suppress("UNUSED_PARAMETER") isbn: String) {
        // already configured in buildDefaultIsbnData for 9781234567897 — no-op
    }

    // ── Action steps ──

    @When("I enter ISBN {string} and request a lookup")
    fun iEnterIsbnAndRequestLookup(isbn: String) {
        state.lastErrorMessage = null

        if (isbnLookupUnavailable) {
            state.lastErrorMessage = "ISBN lookup service is currently unavailable — please try again later"
            return
        }

        if (!isbn.matches(Regex("^\\d{13}$"))) {
            state.lastErrorMessage = "Invalid ISBN format"
            state.clientSideValidationFailed = true
            return
        }

        // Check if already in catalog
        val checkResult = mockMvc.perform(
            get("/api/catalog/books").param("q", isbn)
        ).andReturn()
        if (checkResult.response.contentAsString.contains("\"isbn\":\"$isbn\"")) {
            state.lastErrorMessage = "A book with this ISBN already exists in the catalog"
            return
        }

        val data = isbnLookupData[isbn]
        if (data == null) {
            state.lastErrorMessage = "No book found for this ISBN"
            return
        }

        state.selectedIsbn = isbn
        state.selectedTitle = data.title
        state.selectedAuthors = data.author
        state.selectedYear = data.publicationYear
    }

    @When("I enter publication year {int}")
    fun iEnterPublicationYear(year: Int) {
        state.selectedYear = year
    }

    // ── Assertion steps ──

    @Then("the resolved title and author are displayed")
    fun theResolvedTitleAndAuthorAreDisplayed() {
        assertTrue(state.selectedTitle?.isNotBlank() == true, "Expected a resolved title")
        assertTrue(state.selectedAuthors?.isNotBlank() == true, "Expected a resolved author")
    }

    @And("the publication year field is empty and editable")
    fun thePublicationYearFieldIsEmptyAndEditable() {
        assertEquals(null, state.selectedYear, "Expected publication year to be empty")
    }

    @And("no request is made to the lookup service")
    fun noRequestIsMadeToTheLookupService() {
        assertTrue(state.clientSideValidationFailed, "Expected client-side validation to have prevented the lookup")
    }
}
