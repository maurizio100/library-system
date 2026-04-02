package com.library.catalog.bdd.steps

import com.library.catalog.infra.persistence.BookJpaEntity
import com.library.catalog.infra.persistence.BookJpaRepository
import com.library.catalog.infra.persistence.CopyJpaEntity
import com.library.catalog.infra.persistence.CopyJpaRepository
import io.cucumber.datatable.DataTable
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

class SearchBooksStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var copyJpaRepository: CopyJpaRepository

    private lateinit var lastResult: MvcResult

    @Before
    fun setUp() {
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
    }

    @Given("the catalog contains the following books:")
    fun theCatalogContainsBooks(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            bookJpaRepository.save(
                BookJpaEntity(
                    isbn = row["isbn"]!!,
                    title = row["title"]!!,
                    authors = mutableListOf(row["author"]!!),
                    publicationYear = row["year"]!!.toInt()
                )
            )
        }
    }

    @And("the book with ISBN {string} has {int} copies, {int} Available and {int} Borrowed")
    fun theBookHasCopiesMixed(isbn: String, total: Int, available: Int, borrowed: Int) {
        for (i in 1..available) {
            copyJpaRepository.save(CopyJpaEntity(barcode = "$isbn-A$i", isbn = isbn, status = "Available"))
        }
        for (i in 1..borrowed) {
            copyJpaRepository.save(CopyJpaEntity(barcode = "$isbn-B$i", isbn = isbn, status = "Borrowed"))
        }
    }

    @And("the book with ISBN {string} has {int} copy, {int} Available")
    fun theBookHasCopyAvailable(isbn: String, total: Int, available: Int) {
        for (i in 1..available) {
            copyJpaRepository.save(CopyJpaEntity(barcode = "$isbn-A$i", isbn = isbn, status = "Available"))
        }
    }

    @And("the book with ISBN {string} has {int} copies, {int} Available")
    fun theBookHasCopiesNoneAvailable(isbn: String, total: Int, available: Int) {
        for (i in 1..total) {
            copyJpaRepository.save(CopyJpaEntity(barcode = "$isbn-B$i", isbn = isbn, status = "Borrowed"))
        }
    }

    @When("I search for {string}")
    fun iSearchFor(query: String) {
        lastResult = mockMvc.perform(
            get("/api/catalog/books").param("q", query)
        ).andReturn()
    }

    @Then("the search returns {int} result(s)")
    fun theSearchReturnsNResults(count: Int) {
        val status = lastResult.response.status
        if (count == 0 && status == 200) {
            val body = lastResult.response.contentAsString
            assertEquals("[]", body)
        } else if (status == 200) {
            val body = lastResult.response.contentAsString
            val resultCount = body.split("\"isbn\"").size - 1
            assertEquals(count, resultCount)
        }
    }

    @And("the result contains the book with ISBN {string} and title {string}")
    fun theResultContainsBook(isbn: String, title: String) {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"isbn\":\"$isbn\""), "Expected ISBN $isbn in results: $body")
        assertTrue(body.contains("\"title\":\"$title\""), "Expected title $title in results: $body")
    }

    @And("the result shows {int} available copy/copies for ISBN {string}")
    fun theResultShowsAvailableCopies(count: Int, isbn: String) {
        val body = lastResult.response.contentAsString
        assertTrue(
            body.contains("\"availableCopies\":$count"),
            "Expected availableCopies=$count for ISBN $isbn in: $body"
        )
    }

    @Then("the search is rejected with reason {string}")
    fun theSearchIsRejectedWithReason(reason: String) {
        val status = lastResult.response.status
        assertTrue(status in 400..499, "Expected 4xx but got $status")
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(reason), "Expected '$reason' in: $body")
    }
}
