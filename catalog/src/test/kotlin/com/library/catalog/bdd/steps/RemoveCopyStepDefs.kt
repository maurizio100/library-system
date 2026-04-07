package com.library.catalog.bdd.steps

import com.library.catalog.domain.command.RemoveCopyCommand
import com.library.catalog.domain.command.RemoveCopyHandler
import com.library.catalog.infra.persistence.BookJpaRepository
import com.library.catalog.infra.persistence.CopyJpaEntity
import com.library.catalog.infra.persistence.CopyJpaRepository
import com.library.catalog.bdd.ScenarioState
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoveCopyStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var copyJpaRepository: CopyJpaRepository

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var removeCopyHandler: RemoveCopyHandler

    @Autowired
    private lateinit var scenarioState: ScenarioState

    private var lastException: Exception? = null

    @Given("copy {string} has status {string}")
    fun copyHasStatus(barcode: String, status: String) {
        val existing = copyJpaRepository.findById(barcode).orElseThrow { AssertionError("Copy $barcode not found") }
        copyJpaRepository.save(CopyJpaEntity(barcode = existing.barcode, isbn = existing.isbn, status = status))
    }

    @When("I remove copy {string} from the catalog")
    fun iRemoveCopyFromCatalog(barcode: String) {
        lastException = null
        if (barcode.isBlank()) {
            try {
                removeCopyHandler.handle(RemoveCopyCommand(barcode))
            } catch (e: Exception) {
                lastException = e
            }
            return
        }
        scenarioState.lastMvcResult = mockMvc.perform(delete("/api/catalog/copies/$barcode")).andReturn()
    }

    @Then("the removal is rejected with reason {string}")
    fun theRemovalIsRejectedWithReason(reason: String) {
        val exception = lastException
        if (exception != null) {
            assertTrue(
                exception.message?.contains(reason) == true,
                "Expected '$reason' in exception message: ${exception.message}"
            )
            return
        }
        val result = scenarioState.lastMvcResult!!
        assertTrue(result.response.status in 400..499, "Expected 4xx but got ${result.response.status}")
        assertTrue(
            result.response.contentAsString.contains(reason),
            "Expected '$reason' in response body: ${result.response.contentAsString}"
        )
    }

    @Then("copy {string} no longer exists in the catalog")
    fun copyNoLongerExistsInCatalog(barcode: String) {
        assertNull(copyJpaRepository.findById(barcode).orElse(null), "Expected copy $barcode to be removed")
    }

    @And("copy {string} still exists with status {string}")
    fun copyStillExistsWithStatus(barcode: String, status: String) {
        val copy = copyJpaRepository.findById(barcode).orElse(null)
        assertNotNull(copy, "Expected copy $barcode to exist")
        assertEquals(status, copy.status)
    }

    @Then("the book with ISBN {string} still exists in the catalog")
    fun theBookStillExistsInCatalog(isbn: String) {
        assertTrue(bookJpaRepository.findById(isbn).isPresent, "Expected book $isbn to still exist")
    }
}
