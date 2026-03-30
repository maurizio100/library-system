package com.library.catalog.bdd

import com.library.catalog.infra.persistence.BookJpaRepository
import com.library.catalog.infra.persistence.CopyJpaRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListAllBooksStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var copyJpaRepository: CopyJpaRepository

    private lateinit var lastResult: MvcResult

    private val mapper = jacksonObjectMapper()

    @Given("the catalog contains no books")
    fun theCatalogContainsNoBooks() {
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
    }

    @When("I list all books")
    fun iListAllBooks() {
        lastResult = mockMvc.perform(get("/api/catalog/books")).andReturn()
    }

    @Then("I receive {int} book(s)")
    fun iReceiveNBooks(count: Int) {
        val body = lastResult.response.contentAsString
        val results: List<Map<String, Any>> = mapper.readValue(body)
        assertEquals(count, results.size)
    }

    @Then("the list contains a book with ISBN {string} and title {string}")
    fun theListContainsBook(isbn: String, title: String) {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"isbn\":\"$isbn\""), "Expected ISBN $isbn in results: $body")
        assertTrue(body.contains("\"title\":\"$title\""), "Expected title $title in results: $body")
    }

    @Then("the list shows {int} available copy/copies out of {int} total copy/copies for ISBN {string}")
    fun theListShowsCopyAvailability(available: Int, total: Int, isbn: String) {
        val body = lastResult.response.contentAsString
        val results: List<Map<String, Any>> = mapper.readValue(body)
        val book = results.find { it["isbn"] == isbn }
        assertTrue(book != null, "Expected book with ISBN $isbn in results: $body")
        assertEquals(available, (book["availableCopies"] as Number).toInt())
        assertEquals(total, (book["totalCopies"] as Number).toInt())
    }
}
