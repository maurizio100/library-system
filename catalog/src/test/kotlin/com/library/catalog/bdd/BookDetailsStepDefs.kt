package com.library.catalog.bdd

import com.library.catalog.infra.persistence.BookJpaEntity
import com.library.catalog.infra.persistence.BookJpaRepository
import com.library.catalog.infra.persistence.CopyJpaEntity
import com.library.catalog.infra.persistence.CopyJpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookDetailsStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var copyJpaRepository: CopyJpaRepository

    private lateinit var lastResult: MvcResult
    private var currentIsbn: String = ""

    @Before
    fun setUp() {
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
    }

    @Given("a book with ISBN {string}, title {string}, and author {string} exists in the catalog")
    fun aBookExistsInTheCatalog(isbn: String, title: String, author: String) {
        currentIsbn = isbn
        bookJpaRepository.save(
            BookJpaEntity(isbn = isbn, title = title, authors = mutableListOf(author), publicationYear = 2000)
        )
    }

    @Given("no book with ISBN {string} exists in the catalog")
    fun noBookWithIsbnExistsInCatalog(isbn: String) {
        // @Before already cleared the database; nothing to do
    }

    @And("the book has a copy with barcode {string} that is Available")
    fun theBookHasACopyThatIsAvailable(barcode: String) {
        copyJpaRepository.save(CopyJpaEntity(barcode = barcode, isbn = currentIsbn, status = "Available"))
    }

    @And("the book has a copy with barcode {string} that is Borrowed")
    fun theBookHasACopyThatIsBorrowed(barcode: String) {
        copyJpaRepository.save(CopyJpaEntity(barcode = barcode, isbn = currentIsbn, status = "Borrowed"))
    }

    @And("the book has no copies")
    fun theBookHasNoCopies() {
        // nothing to do — no copies are added
    }

    @When("the librarian navigates to the details page for ISBN {string}")
    fun theLibrarianNavigatesToTheDetailsPage(isbn: String) {
        lastResult = mockMvc.perform(get("/api/catalog/books/$isbn")).andReturn()
    }

    @Then("the page displays the title {string}")
    fun thePageDisplaysTheTitle(title: String) {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"title\":\"$title\""), "Expected title '$title' in: $body")
    }

    @And("the page displays the author {string}")
    fun thePageDisplaysTheAuthor(author: String) {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(author), "Expected author '$author' in: $body")
    }

    @And("the page displays the ISBN {string}")
    fun thePageDisplaysTheIsbn(isbn: String) {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"isbn\":\"$isbn\""), "Expected ISBN '$isbn' in: $body")
    }

    @And("the copies list shows barcode {string} with status {string}")
    fun theCopiesListShowsBarcodeWithStatus(barcode: String, status: String) {
        val body = lastResult.response.contentAsString
        assertTrue(
            body.contains("\"barcode\":\"$barcode\"") && body.contains("\"status\":\"$status\""),
            "Expected barcode '$barcode' with status '$status' in: $body"
        )
    }

    @And("the copies list shows a message indicating no copies are registered")
    fun theCopiesListShowsNoCopiesMessage() {
        assertEquals(200, lastResult.response.status)
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"copies\":[]"), "Expected empty copies array in: $body")
    }

    @Then("the page displays a message indicating the book was not found")
    fun thePageDisplaysBookNotFoundMessage() {
        assertEquals(404, lastResult.response.status)
        val body = lastResult.response.contentAsString
        assertTrue(body.isNotEmpty(), "Expected error response body but got empty")
    }
}
