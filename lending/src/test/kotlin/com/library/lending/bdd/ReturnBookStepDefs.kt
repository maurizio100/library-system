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
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReturnBookStepDefs {

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
    private var currentLoanId: String = "00000000-0000-0000-0000-000000000001"

    @Before
    fun setUp() {
        loanJpaRepository.deleteAll()
        copyJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
        memberJpaRepository.deleteAll()
        testEventListener.clear()
    }

    @Given("a member {string} with Member ID {string}")
    fun aMemberWithMemberId(name: String, memberId: String) {
        currentMemberId = memberId
        memberJpaRepository.save(
            MemberJpaEntity(
                memberId = memberId,
                name = name,
                email = "${name.lowercase().replace(" ", ".")}@example.com",
                borrowingLimit = 3,
                activeLoansCount = 1
            )
        )
    }

    @And("the catalog contains a book with ISBN {string} titled {string}")
    fun theCatalogContainsABookWithIsbn(isbn: String, title: String) {
        bookJpaRepository.save(
            BookJpaEntity(isbn = isbn, title = title, authors = mutableListOf("Author"), publicationYear = 2020)
        )
    }

    @And("the book has a copy with barcode {string}")
    fun theBookHasACopyWithBarcode(barcode: String) {
        val book = bookJpaRepository.findAll().first()
        copyJpaRepository.save(CopyJpaEntity(barcode = barcode, isbn = book.isbn, status = "Borrowed"))
    }

    @Given("{string} has an active loan for copy {string} created {int} days ago")
    fun memberHasActiveLoan(name: String, barcode: String, daysAgo: Int) {
        val loanDate = LocalDate.now().minusDays(daysAgo.toLong())
        val dueDate = loanDate.plusDays(14)
        loanJpaRepository.save(
            LoanJpaEntity(
                loanId = currentLoanId,
                memberId = currentMemberId,
                copyBarcode = barcode,
                loanDate = loanDate,
                dueDate = dueDate,
                status = "Active"
            )
        )
    }

    @Given("{string} has an overdue loan for copy {string} that is {int} days overdue")
    fun memberHasOverdueLoan(name: String, barcode: String, daysOverdue: Int) {
        val dueDate = LocalDate.now().minusDays(daysOverdue.toLong())
        val loanDate = dueDate.minusDays(14)
        loanJpaRepository.save(
            LoanJpaEntity(
                loanId = currentLoanId,
                memberId = currentMemberId,
                copyBarcode = barcode,
                loanDate = loanDate,
                dueDate = dueDate,
                status = "Active"
            )
        )
    }

    @Given("{string} has an overdue loan for copy {string} that is {int} day overdue")
    fun memberHasOverdueLoanSingular(name: String, barcode: String, daysOverdue: Int) {
        memberHasOverdueLoan(name, barcode, daysOverdue)
    }

    @Given("{string} had a loan for copy {string} that was already returned")
    fun memberHadReturnedLoan(name: String, barcode: String) {
        val loanDate = LocalDate.now().minusDays(10)
        val dueDate = loanDate.plusDays(14)
        loanJpaRepository.save(
            LoanJpaEntity(
                loanId = currentLoanId,
                memberId = currentMemberId,
                copyBarcode = barcode,
                loanDate = loanDate,
                dueDate = dueDate,
                status = "Returned",
                returnDate = LocalDate.now().minusDays(3)
            )
        )
        // Copy is already Available since it was returned
        val copy = copyJpaRepository.findById(barcode).get()
        copyJpaRepository.save(CopyJpaEntity(barcode = copy.barcode, isbn = copy.isbn, status = "Available"))

        // Member has no active loans since it was returned
        val member = memberJpaRepository.findById(currentMemberId).get()
        memberJpaRepository.save(
            MemberJpaEntity(
                memberId = member.memberId,
                name = member.name,
                email = member.email,
                borrowingLimit = member.borrowingLimit,
                activeLoansCount = 0
            )
        )
    }

    @When("{string} returns copy {string}")
    fun memberReturnsCopy(name: String, barcode: String) {
        lastResult = mockMvc.perform(
            post("/api/lending/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"memberId":"$currentMemberId","copyBarcode":"$barcode"}""")
        ).andReturn()
    }

    @When("member {string} returns copy {string}")
    fun memberByIdReturnsCopy(memberId: String, barcode: String) {
        lastResult = mockMvc.perform(
            post("/api/lending/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"memberId":"$memberId","copyBarcode":"$barcode"}""")
        ).andReturn()
    }

    @Then("the loan is marked as Returned")
    fun theLoanIsMarkedAsReturned() {
        assertEquals(200, lastResult.response.status)
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"loanId\""))
    }

    @And("the return date is today")
    fun theReturnDateIsToday() {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(LocalDate.now().toString()))
    }

    @And("no Fee is charged")
    fun noFeeIsCharged() {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"fee\":null"), "Expected no fee but got: $body")
        assertEquals(0, testEventListener.getFeeChargedEvents().size)
    }

    @And("a BookReturned event is published with the loan ID, Member ID {string}, copy barcode {string}, and today's date")
    fun bookReturnedEventPublished(memberId: String, barcode: String) {
        val events = testEventListener.getBookReturnedEvents()
        assertTrue(events.isNotEmpty(), "Expected BookReturned event but none found")
        val event = events.last()
        assertEquals(memberId, event.memberId)
        assertEquals(barcode, event.copyBarcode)
        assertEquals(LocalDate.now(), event.returnDate)
    }

    @And("copy {string} is marked as Available")
    fun copyIsMarkedAsAvailable(barcode: String) {
        val copy = copyJpaRepository.findById(barcode).get()
        assertEquals("Available", copy.status)
    }

    @And("a Fee of €{double} is charged to {string}")
    fun feeIsCharged(amount: Double, name: String) {
        val body = lastResult.response.contentAsString
        val expectedAmount = BigDecimal(amount).setScale(2)
        assertTrue(body.contains("\"fee\":$expectedAmount") || body.contains("\"fee\":${amount}"),
            "Expected fee $expectedAmount in response: $body")
    }

    @And("a FeeCharged event is published with the loan ID, Member ID {string}, amount €{double}, and {int} days overdue")
    fun feeChargedEventPublished(memberId: String, amount: Double, daysOverdue: Int) {
        val events = testEventListener.getFeeChargedEvents()
        assertTrue(events.isNotEmpty(), "Expected FeeCharged event but none found")
        val event = events.last()
        assertEquals(memberId, event.memberId)
        assertEquals(BigDecimal(amount).setScale(2), event.amount.setScale(2))
        assertEquals(daysOverdue.toLong(), event.daysOverdue)
    }

    @And("a FeeCharged event is published with the loan ID, Member ID {string}, amount €{double}, and {int} day overdue")
    fun feeChargedEventPublishedSingular(memberId: String, amount: Double, daysOverdue: Int) {
        feeChargedEventPublished(memberId, amount, daysOverdue)
    }

    @Then("the return is rejected with reason {string}")
    fun theReturnIsRejectedWithReason(reason: String) {
        val status = lastResult.response.status
        assertTrue(status in 400..499, "Expected 4xx status but got $status")
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(reason), "Expected '$reason' in response body: $body")
    }

    @And("copy {string} remains Available")
    fun copyRemainsAvailable(barcode: String) {
        val copy = copyJpaRepository.findById(barcode).get()
        assertEquals("Available", copy.status)
    }
}
