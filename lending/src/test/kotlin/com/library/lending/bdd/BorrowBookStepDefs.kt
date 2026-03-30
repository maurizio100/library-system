package com.library.lending.bdd

import com.library.catalog.infra.persistence.BookJpaEntity
import com.library.catalog.infra.persistence.BookJpaRepository
import com.library.catalog.infra.persistence.CopyJpaEntity
import com.library.catalog.infra.persistence.CopyJpaRepository
import com.library.lending.infra.persistence.LoanJpaEntity
import com.library.lending.infra.persistence.LoanJpaRepository
import com.library.lending.infra.persistence.MemberJpaEntity
import com.library.lending.infra.persistence.MemberJpaRepository
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
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BorrowBookStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @Autowired
    private lateinit var loanJpaRepository: LoanJpaRepository

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var copyJpaRepository: CopyJpaRepository

    @Autowired
    private lateinit var testEventListener: TestEventListener

    private lateinit var lastResult: MvcResult
    private var currentMemberId: String = ""

    @Before
    fun setUp() {
        loanJpaRepository.deleteAll()
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
        memberJpaRepository.deleteAll()
        testEventListener.clear()
    }

    @Given("a member {string} with member ID {string} and borrowing limit {int}")
    fun aMemberWithIdAndLimit(name: String, memberId: String, limit: Int) {
        currentMemberId = memberId
        memberJpaRepository.save(MemberJpaEntity(memberId = memberId, name = name, borrowingLimit = limit, activeLoansCount = 0))
    }

    @And("the catalog contains a book with ISBN {string} and an available copy with barcode {string}")
    fun theCatalogContainsBookWithAvailableCopy(isbn: String, barcode: String) {
        bookJpaRepository.save(BookJpaEntity(isbn = isbn, title = "Test Book", authors = mutableListOf("Author"), publicationYear = 2020))
        copyJpaRepository.save(CopyJpaEntity(barcode = barcode, isbn = isbn, status = "Available"))
    }

    @Given("{string} has {int} active loans")
    fun memberHasActiveLoans(name: String, count: Int) {
        if (count > 0) {
            val member = memberJpaRepository.findById(currentMemberId).get()
            memberJpaRepository.save(MemberJpaEntity(memberId = member.memberId, name = member.name, borrowingLimit = member.borrowingLimit, activeLoansCount = count))
            for (i in 1..count) {
                loanJpaRepository.save(
                    LoanJpaEntity(
                        loanId = "existing-loan-$i",
                        memberId = currentMemberId,
                        copyBarcode = "EXISTING-$i",
                        loanDate = LocalDate.now(),
                        dueDate = LocalDate.now().plusDays(14),
                        status = "Active"
                    )
                )
            }
        }
    }

    @Given("the copy with barcode {string} is already Borrowed")
    fun theCopyIsAlreadyBorrowed(barcode: String) {
        val copy = copyJpaRepository.findById(barcode).get()
        copyJpaRepository.save(CopyJpaEntity(barcode = copy.barcode, isbn = copy.isbn, status = "Borrowed"))
    }

    @When("{string} borrows the copy with barcode {string}")
    fun memberBorrowsCopy(name: String, barcode: String) {
        lastResult = mockMvc.perform(
            post("/api/lending/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"memberId":"$currentMemberId","copyBarcode":"$barcode"}""")
        ).andReturn()
    }

    @When("a member with ID {string} borrows the copy with barcode {string}")
    fun unknownMemberBorrowsCopy(memberId: String, barcode: String) {
        lastResult = mockMvc.perform(
            post("/api/lending/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"memberId":"$memberId","copyBarcode":"$barcode"}""")
        ).andReturn()
    }

    @Then("a loan is created for member {string} and copy {string}")
    fun aLoanIsCreated(memberId: String, barcode: String) {
        assertEquals(201, lastResult.response.status)
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"memberId\":\"$memberId\""))
        assertTrue(body.contains("\"copyBarcode\":\"$barcode\""))
    }

    @And("the loan due date is {int} days from today")
    fun theLoanDueDateIsDaysFromToday(days: Int) {
        val expectedDueDate = LocalDate.now().plusDays(days.toLong())
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(expectedDueDate.toString()))
    }

    @And("a LoanCreated event is published with member {string} and copy {string}")
    fun loanCreatedEventPublished(memberId: String, barcode: String) {
        val events = testEventListener.getLoanCreatedEvents()
        assertEquals(1, events.size)
        assertEquals(memberId, events[0].memberId)
        assertEquals(barcode, events[0].copyBarcode)
    }

    @And("the copy {string} is marked as Borrowed in the catalog")
    fun theCopyIsMarkedAsBorrowed(barcode: String) {
        val copy = copyJpaRepository.findById(barcode).get()
        assertEquals("Borrowed", copy.status)
    }

    @Then("the loan is rejected with reason {string}")
    fun theLoanIsRejectedWithReason(reason: String) {
        val status = lastResult.response.status
        assertTrue(status in 400..499, "Expected 4xx status but got $status")
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(reason), "Expected '$reason' in response body: $body")
    }

    @And("no LoanCreated event is published")
    fun noLoanCreatedEventPublished() {
        assertEquals(0, testEventListener.getLoanCreatedEvents().size)
    }

    @And("the copy {string} remains Available")
    fun theCopyRemainsAvailable(barcode: String) {
        val copy = copyJpaRepository.findById(barcode).get()
        assertEquals("Available", copy.status)
    }
}
