package com.library.catalog.bdd

import com.library.catalog.infra.persistence.BookJpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddBookStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var testEventListener: TestEventListener

    @Autowired
    private lateinit var scenarioState: ScenarioState

    @Before
    fun setUp() {
        bookJpaRepository.deleteAll()
        testEventListener.clear()
        scenarioState.clear()
    }

    @Given("the catalog does not contain a book with ISBN {string}")
    fun theCatalogDoesNotContainBookWithIsbn(isbn: String) {
        assertTrue(bookJpaRepository.findById(isbn).isEmpty)
    }

    @Given("the catalog contains a book with ISBN {string} and title {string}")
    fun theCatalogContainsBookWithIsbnAndTitle(isbn: String, title: String) {
        mockMvc.perform(
            post("/api/catalog/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"isbn":"$isbn","title":"$title","authors":["Author"],"publicationYear":2020}""")
        )
        testEventListener.clear()
    }

    @When("I add a book with ISBN {string}, title {string}, author {string}, and publication year {int}")
    fun iAddBookWithDetails(isbn: String, title: String, author: String, publicationYear: Int) {
        val result = mockMvc.perform(
            post("/api/catalog/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"isbn":"$isbn","title":"$title","authors":["$author"],"publicationYear":$publicationYear}""")
        ).andReturn()
        scenarioState.lastMvcResult = result
    }

    @Then("the catalog contains a book with ISBN {string}")
    fun theCatalogContainsBookWithIsbn(isbn: String) {
        assertTrue(bookJpaRepository.findById(isbn).isPresent)
    }

    @Then("the book has title {string}")
    fun theBookHasTitle(title: String) {
        val body = scenarioState.lastMvcResult!!.response.contentAsString
        assertTrue(body.contains("\"title\":\"$title\""))
    }

    @Then("the book has author {string}")
    fun theBookHasAuthor(author: String) {
        val body = scenarioState.lastMvcResult!!.response.contentAsString
        assertTrue(body.contains("\"$author\""))
    }

    @And("a BookAdded event is published with ISBN {string}")
    fun bookAddedEventPublished(isbn: String) {
        val events = testEventListener.getBookAddedEvents()
        assertEquals(1, events.size)
        assertEquals(isbn, events[0].isbn)
    }

    @Then("the book is rejected with reason {string}")
    fun theBookIsRejectedWithReason(reason: String) {
        val status = scenarioState.lastMvcResult!!.response.status
        assertTrue(status in 400..499, "Expected 4xx status but got $status")
        val body = scenarioState.lastMvcResult!!.response.contentAsString
        assertTrue(body.contains(reason), "Expected '$reason' in response body: $body")
    }

    @And("the catalog still contains the original book with title {string}")
    fun theCatalogStillContainsOriginalBook(title: String) {
        val books = bookJpaRepository.findAll()
        assertTrue(books.any { it.title == title })
    }
}
