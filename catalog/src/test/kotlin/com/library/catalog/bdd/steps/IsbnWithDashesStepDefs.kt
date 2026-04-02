package com.library.catalog.bdd.steps

import com.library.catalog.bdd.ScenarioState
import com.library.catalog.infra.persistence.BookJpaRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.assertTrue

class IsbnWithDashesStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var scenarioState: ScenarioState

    @Given("a book with ISBN {string} exists in the catalog")
    fun aBookWithIsbnExistsInCatalog(isbn: String) {
        mockMvc.perform(
            post("/api/catalog/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"isbn":"$isbn","title":"The Critique of Pure Reason","authors":["Immanuel Kant"],"publicationYear":1781}""")
        )
    }

    @Given("no book exists in the catalog")
    fun noBookExistsInCatalog() {
        // @Before in AddBookStepDefs already clears the repository before each scenario
    }

    @When("the librarian adds a book with ISBN {string}, title {string}, author {string}, and publication year {int}")
    fun theLibrarianAddsBookWithDetails(isbn: String, title: String, author: String, publicationYear: Int) {
        val result = mockMvc.perform(
            post("/api/catalog/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"isbn":"$isbn","title":"$title","authors":["$author"],"publicationYear":$publicationYear}""")
        ).andReturn()
        scenarioState.lastMvcResult = result
    }

    @Then("the book is added to the catalog with the normalised ISBN {string}")
    fun theBookIsAddedWithNormalisedIsbn(normalisedIsbn: String) {
        assertTrue(bookJpaRepository.findById(normalisedIsbn).isPresent)
    }

    @When("the librarian looks up the book by ISBN {string}")
    fun theLibrarianLooksUpBookByIsbn(isbn: String) {
        val result = mockMvc.perform(get("/api/catalog/books/$isbn")).andReturn()
        scenarioState.lastMvcResult = result
    }

    @Then("the book with title {string} is returned")
    fun theBookWithTitleIsReturned(title: String) {
        val status = scenarioState.lastMvcResult!!.response.status
        assertTrue(status == 200, "Expected 200 but got $status")
        val body = scenarioState.lastMvcResult!!.response.contentAsString
        assertTrue(body.contains("\"title\":\"$title\""), "Expected title '$title' in: $body")
    }

    @Then("the system rejects the request with the message {string}")
    fun theSystemRejectsWithMessage(message: String) {
        val status = scenarioState.lastMvcResult!!.response.status
        assertTrue(status in 400..499, "Expected 4xx status but got $status")
        val body = scenarioState.lastMvcResult!!.response.contentAsString
        assertTrue(body.contains(message), "Expected '$message' in response body: $body")
    }
}
