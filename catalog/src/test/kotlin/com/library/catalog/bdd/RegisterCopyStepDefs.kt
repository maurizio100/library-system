package com.library.catalog.bdd

import com.library.catalog.infra.persistence.BookJpaRepository
import com.library.catalog.infra.persistence.CopyJpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterCopyStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var copyJpaRepository: CopyJpaRepository

    @Autowired
    private lateinit var testEventListener: TestEventListener

    private lateinit var lastResult: MvcResult

    @Before
    fun setUp() {
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
        testEventListener.clear()
    }

    @Given("a copy with barcode {string} exists for book with ISBN {string}")
    fun aCopyWithBarcodeExistsForBook(barcode: String, isbn: String) {
        mockMvc.perform(
            post("/api/catalog/books/$isbn/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"barcode":"$barcode"}""")
        )
        testEventListener.clear()
    }

    @When("I register a copy with barcode {string} for book with ISBN {string}")
    fun iRegisterCopyWithBarcode(barcode: String, isbn: String) {
        lastResult = mockMvc.perform(
            post("/api/catalog/books/$isbn/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"barcode":"$barcode"}""")
        ).andReturn()
    }

    @Then("the book with ISBN {string} has {int} copy/copies")
    fun theBookHasNCopies(isbn: String, count: Int) {
        val copies = copyJpaRepository.findByIsbn(isbn)
        assertEquals(count, copies.size)
    }

    @And("the copy with barcode {string} has status {string}")
    fun theCopyHasStatus(barcode: String, status: String) {
        val copy = copyJpaRepository.findById(barcode).orElse(null)
        assertTrue(copy != null, "Copy with barcode $barcode not found")
        assertEquals(status, copy.status)
    }

    @And("a CopyRegistered event is published with barcode {string} and ISBN {string}")
    fun copyRegisteredEventPublished(barcode: String, isbn: String) {
        val events = testEventListener.getCopyRegisteredEvents()
        assertEquals(1, events.size)
        assertEquals(barcode, events[0].barcode)
        assertEquals(isbn, events[0].isbn)
    }

    @Then("the copy is rejected with reason {string}")
    fun theCopyIsRejectedWithReason(reason: String) {
        val status = lastResult.response.status
        assertTrue(status in 400..499, "Expected 4xx status but got $status")
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(reason), "Expected '$reason' in response body: $body")
    }
}
