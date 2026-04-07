package com.library.catalog.bdd.steps

import com.library.catalog.infra.persistence.BookJpaEntity
import com.library.catalog.infra.persistence.BookJpaRepository
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
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class BookCoverImageStepDefs {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var bookJpaRepository: BookJpaRepository
    @Autowired private lateinit var copyJpaRepository: CopyJpaRepository

    private lateinit var lastResult: MvcResult

    @Before
    fun setUp() {
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
    }

    @Given("the book {string} was added to the catalog via ISBN lookup")
    fun theBookWasAddedViaCatalogIsbnLookup(isbn: String) {
        val normalised = isbn.replace("-", "")
        bookJpaRepository.save(
            BookJpaEntity(isbn = normalised, title = "Effective Java", authors = mutableListOf("Joshua Bloch"), publicationYear = 2018)
        )
    }

    @And("the ISBN lookup returned a cover image for {string}")
    fun theIsbnLookupReturnedACoverImageFor(isbn: String) {
        val normalised = isbn.replace("-", "")
        val existing = bookJpaRepository.findById(normalised).get()
        bookJpaRepository.save(
            BookJpaEntity(
                isbn = existing.isbn,
                title = existing.title,
                authors = existing.authors,
                publicationYear = existing.publicationYear,
                coverUrl = "https://covers.openlibrary.org/b/id/12345-L.jpg"
            )
        )
    }

    @Given("the book {string} was added to the catalog manually without a cover image")
    fun theBookWasAddedManuallyWithoutACoverImage(isbn: String) {
        val normalised = isbn.replace("-", "")
        bookJpaRepository.save(
            BookJpaEntity(isbn = normalised, title = "Effective Java", authors = mutableListOf("Joshua Bloch"), publicationYear = 2018)
        )
    }

    @And("the ISBN lookup returned no cover image for {string}")
    fun theIsbnLookupReturnedNoCoverImageFor(@Suppress("UNUSED_PARAMETER") isbn: String) {
        // book already saved without a coverUrl — no-op
    }

    @When("the librarian opens the details page for {string}")
    fun theLibrarianOpensTheDetailsPageFor(isbn: String) {
        val normalised = isbn.replace("-", "")
        lastResult = mockMvc.perform(get("/api/catalog/books/$normalised")).andReturn()
    }

    @Then("the cover image is displayed on the page")
    fun theCoverImageIsDisplayedOnThePage() {
        val body = lastResult.response.contentAsString
        assertTrue(
            body.contains("\"coverUrl\":\"https://covers.openlibrary.org"),
            "Expected a non-null coverUrl in: $body"
        )
    }

    @Then("no cover image is displayed")
    fun noCoverImageIsDisplayed() {
        val body = lastResult.response.contentAsString
        assertFalse(
            body.contains("\"coverUrl\":\"https://"),
            "Expected coverUrl to be null or absent in: $body"
        )
    }

    @And("a placeholder is shown instead")
    fun aPlaceholderIsShownInstead() {
        // placeholder rendering is frontend-only — verify API returns null coverUrl
        val body = lastResult.response.contentAsString
        assertTrue(
            body.contains("\"coverUrl\":null"),
            "Expected coverUrl:null in: $body"
        )
    }
}
