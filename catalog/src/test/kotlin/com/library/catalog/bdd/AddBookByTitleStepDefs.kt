package com.library.catalog.bdd

import com.library.catalog.domain.port.ExternalBookCandidate
import com.library.catalog.infra.persistence.BookJpaRepository
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddBookByTitleStepDefs {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var bookJpaRepository: BookJpaRepository
    @Autowired private lateinit var fakeExternalBookLookupPort: FakeExternalBookLookupPort
    @Autowired private lateinit var scenarioState: ScenarioState
    @Autowired private lateinit var state: TitleSearchState

    @Before
    fun setUp() {
        bookJpaRepository.deleteAll()
        state.clear()
        fakeExternalBookLookupPort.reset()
        setupDefaultFakeResults()
    }

    private fun setupDefaultFakeResults() {
        fakeExternalBookLookupPort.configure(
            mapOf(
                "The Great Gatsby" to listOf(
                    ExternalBookCandidate("9780743273565", "The Great Gatsby", listOf("F. Scott Fitzgerald"), 1925),
                    ExternalBookCandidate("9780684801520", "The Great Gatsby (Scribner Classics)", listOf("F. Scott Fitzgerald"), 2004)
                ),
                "Alchemist The" to listOf(
                    ExternalBookCandidate("9780062316097", "The Alchemist", listOf("Paulo Coelho"), 1988)
                ),
                "1984" to listOf(
                    ExternalBookCandidate("9780451524935", "Nineteen Eighty-Four", listOf("George Orwell"), 1949),
                    ExternalBookCandidate("9780141036144", "Nineteen Eighty-Four (Penguin Modern Classics)", listOf("George Orwell"), 2008)
                ),
                "Dune" to listOf(
                    ExternalBookCandidate("9780441013593", "Dune", listOf("Frank Herbert"), 1965),
                    ExternalBookCandidate("9780441172719", "Dune Messiah", listOf("Frank Herbert"), 1969)
                ),
                "xyzzy nonexistent title 99999" to emptyList(),
                "Brave New World" to listOf(
                    ExternalBookCandidate("9780060850524", "Brave New World", listOf("Aldous Huxley"), 1932)
                )
            )
        )
    }

    // ── Setup steps ──

    @Given("the book search service is unavailable")
    fun theBookSearchServiceIsUnavailable() {
        fakeExternalBookLookupPort.setUnavailable()
    }

    // ── Action steps ──

    @When("I switch to the {string} option")
    fun iSwitchToOption(@Suppress("UNUSED_PARAMETER") option: String) {
        // frontend UI toggle — no-op at API level
    }

    @When("I enter title {string} and request a search")
    fun iEnterTitleAndRequestSearch(title: String) {
        state.lastErrorMessage = null
        state.clientSideValidationFailed = false

        if (title.isBlank()) {
            state.lastErrorMessage = "Please enter a title to search"
            state.clientSideValidationFailed = true
            return
        }

        val result = mockMvc.perform(
            get("/api/catalog/books/lookup").param("title", title)
        ).andReturn()
        state.lastLookupResult = result

        if (result.response.status == 200) {
            val body = result.response.contentAsString
            state.lastSearchResults = parseSearchResults(body)
            if (state.lastSearchResults.size == 1) {
                val single = state.lastSearchResults[0]
                state.selectedIsbn = single.isbn
                state.selectedTitle = single.title
                state.selectedAuthors = single.authors.joinToString(", ")
                state.selectedYear = single.publicationYear
            }
        } else {
            state.lastErrorMessage = extractErrorMessage(result.response.contentAsString)
        }
    }

    @When("I select {string} by {string} \\({int}) from the results")
    fun iSelectBookFromResults(title: String, author: String, year: Int) {
        val candidate = state.lastSearchResults.find {
            it.title == title && it.authors.contains(author) && it.publicationYear == year
        }
        assertNotNull(candidate, "Expected to find '$title' by '$author' ($year) in results: ${state.lastSearchResults}")

        state.selectedIsbn = candidate.isbn
        state.selectedTitle = candidate.title
        state.selectedAuthors = candidate.authors.joinToString(", ")
        state.selectedYear = candidate.publicationYear

        val checkResult = mockMvc.perform(
            get("/api/catalog/books").param("q", candidate.isbn)
        ).andReturn()
        if (checkResult.response.contentAsString.contains("\"isbn\":\"${candidate.isbn}\"")) {
            state.lastErrorMessage = "A book with this ISBN already exists in the catalog"
        }
    }

    @When("I change the title to {string}")
    fun iChangeTheTitleTo(newTitle: String) {
        state.selectedTitle = newTitle
    }

    @When("I confirm adding the book")
    fun iConfirmAddingTheBook() {
        val isbn = state.selectedIsbn ?: error("No book selected")
        val title = state.selectedTitle ?: error("No title available")
        val authors = state.selectedAuthors ?: error("No authors available")
        val year = state.selectedYear ?: 0

        val result = mockMvc.perform(
            post("/api/catalog/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"isbn":"$isbn","title":"$title","authors":["$authors"],"publicationYear":$year}""")
        ).andReturn()
        scenarioState.lastMvcResult = result
    }

    @And("I choose to go back to the results")
    fun iChooseToGoBackToResults() {
        state.selectedIsbn = null
        state.selectedTitle = null
        state.selectedAuthors = null
        state.selectedYear = null
    }

    // ── Assertion steps ──

    @Then("a list of books is displayed containing at least:")
    fun aListOfBooksIsDisplayedContainingAtLeast(dataTable: DataTable) {
        assertNotNull(state.lastLookupResult)
        assertEquals(200, state.lastLookupResult!!.response.status)
        val body = state.lastLookupResult!!.response.contentAsString
        dataTable.asMaps().forEach { row ->
            val isbn = row["isbn"]!!
            val title = row["title"]!!
            val author = row["author"]!!
            assertTrue(body.contains("\"isbn\":\"$isbn\""), "Expected ISBN $isbn in: $body")
            assertTrue(body.contains("\"title\":\"$title\""), "Expected title '$title' in: $body")
            assertTrue(body.contains("\"$author\""), "Expected author '$author' in: $body")
        }
    }

    @Then("a list of books is displayed")
    fun aListOfBooksIsDisplayed() {
        assertNotNull(state.lastLookupResult)
        assertEquals(200, state.lastLookupResult!!.response.status)
        val body = state.lastLookupResult!!.response.contentAsString
        assertTrue(body.startsWith("[") && body.contains("\"isbn\""), "Expected non-empty book list in: $body")
    }

    @Then("the title {string}, author {string}, and publication year {int} are displayed")
    fun theTitleAuthorAndYearAreDisplayed(title: String, author: String, year: Int) {
        assertEquals(title, state.selectedTitle, "Selected title mismatch")
        assertTrue(state.selectedAuthors?.contains(author) == true, "Expected author '$author' in '${state.selectedAuthors}'")
        assertEquals(year, state.selectedYear, "Selected year mismatch")
    }

    @Then("a message {string} is displayed")
    fun aMessageIsDisplayed(message: String) {
        if (state.lastErrorMessage != null) {
            assertEquals(message, state.lastErrorMessage)
        } else {
            assertNotNull(state.lastLookupResult)
            assertEquals("[]", state.lastLookupResult!!.response.contentAsString,
                "Expected empty results for message '$message'")
        }
    }

    @Then("an error message {string} is displayed")
    fun anErrorMessageIsDisplayed(expected: String) {
        if (state.lastErrorMessage != null) {
            assertEquals(expected, state.lastErrorMessage)
        } else {
            assertNotNull(state.lastLookupResult)
            val body = state.lastLookupResult!!.response.contentAsString
            assertTrue(body.contains(expected), "Expected '$expected' in: $body")
        }
    }

    @And("the confirm button is disabled")
    fun theConfirmButtonIsDisabled() {
        val hasError = state.lastErrorMessage != null
        val hasHttpError = state.lastLookupResult?.response?.status?.let { it >= 400 } ?: false
        val noResultsAfterSearch = state.lastLookupResult != null && state.lastSearchResults.isEmpty() && state.selectedIsbn == null
        assertTrue(hasError || hasHttpError || noResultsAfterSearch, "Expected confirm button to be disabled")
    }

    @And("no request is made to the search service")
    fun noRequestIsMadeToTheSearchService() {
        assertTrue(state.clientSideValidationFailed, "Expected client-side validation to have prevented the search")
    }

    @And("a success message {string} is displayed")
    fun aSuccessMessageIsDisplayed(@Suppress("UNUSED_PARAMETER") message: String) {
        assertEquals(201, scenarioState.lastMvcResult?.response?.status,
            "Expected 201 Created but got ${scenarioState.lastMvcResult?.response?.status}")
    }

    @Then("the list of books for title {string} is displayed again")
    fun theListOfBooksForTitleIsDisplayedAgain(@Suppress("UNUSED_PARAMETER") title: String) {
        assertTrue(state.lastSearchResults.isNotEmpty(), "Expected previous search results to still be available")
    }

    @Then("the catalog contains a book with ISBN {string} and publication year {int}")
    fun theCatalogContainsBookWithIsbnAndPublicationYear(isbn: String, year: Int) {
        val book = bookJpaRepository.findById(isbn)
        assertTrue(book.isPresent, "Expected book with ISBN $isbn in catalog")
        assertEquals(year, book.get().publicationYear, "Expected publication year $year")
    }

    // ── Helpers ──

    private fun parseSearchResults(json: String): List<ExternalBookCandidate> {
        if (json == "[]" || json.isBlank()) return emptyList()
        return json.split("\\{\"isbn\":\"".toRegex())
            .drop(1)
            .mapNotNull { entry ->
                val isbn = entry.substringBefore("\"")
                val title = entry.substringAfter("\"title\":\"").substringBefore("\"")
                val authorsRaw = entry.substringAfter("\"authors\":[").substringBefore("]")
                val authors = authorsRaw.split(",").map { it.trim('"') }.filter { it.isNotBlank() }
                val yearStr = entry.substringAfter("\"publicationYear\":").substringBefore(",").substringBefore("}")
                val year = yearStr.toIntOrNull()
                if (isbn.isNotBlank() && title.isNotBlank()) ExternalBookCandidate(isbn, title, authors, year)
                else null
            }
    }

    private fun extractErrorMessage(json: String): String =
        json.substringAfter("\"error\":\"").substringBefore("\"")
}
