package com.library.lending.bdd

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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RegisterMemberStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @Autowired
    private lateinit var testEventListener: TestEventListener

    private lateinit var lastResult: MvcResult

    @Before
    fun setUp() {
        memberJpaRepository.deleteAll()
        testEventListener.clear()
    }

    @Given("no member is registered with email {string}")
    fun noMemberRegisteredWithEmail(email: String) {
        val existing = memberJpaRepository.findByEmail(email)
        if (existing != null) {
            memberJpaRepository.delete(existing)
        }
    }

    @Given("a member is already registered with email {string}")
    fun aMemberIsAlreadyRegisteredWithEmail(email: String) {
        memberJpaRepository.save(
            MemberJpaEntity(
                memberId = "existing-member-1",
                name = "Existing Member",
                email = email,
                borrowingLimit = 3,
                activeLoansCount = 0
            )
        )
    }

    @When("I register a member with name {string} and email {string}")
    fun iRegisterAMemberWithNameAndEmail(name: String, email: String) {
        lastResult = mockMvc.perform(
            post("/api/lending/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"$name","email":"$email"}""")
        ).andReturn()
    }

    @Then("a Member is created with name {string} and email {string}")
    fun aMemberIsCreatedWithNameAndEmail(name: String, email: String) {
        assertEquals(201, lastResult.response.status)
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"name\":\"$name\""))
        assertTrue(body.contains("\"email\":\"$email\""))
    }

    @And("the Member is assigned a Member ID")
    fun theMemberIsAssignedAMemberId() {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"memberId\":\""))
    }

    @And("the Member has a borrowing limit of {int}")
    fun theMemberHasABorrowingLimitOf(limit: Int) {
        val body = lastResult.response.contentAsString
        assertTrue(body.contains("\"borrowingLimit\":$limit"))
    }

    @And("a MemberRegistered event is published with the Member ID, name {string}, and email {string}")
    fun memberRegisteredEventPublished(name: String, email: String) {
        val events = testEventListener.getMemberRegisteredEvents()
        assertEquals(1, events.size)
        assertEquals(name, events[0].name)
        assertEquals(email, events[0].email)
        assertNotNull(events[0].memberId)
    }

    @Then("the registration is rejected with reason {string}")
    fun theRegistrationIsRejectedWithReason(reason: String) {
        val status = lastResult.response.status
        assertTrue(status in 400..499, "Expected 4xx status but got $status")
        val body = lastResult.response.contentAsString
        assertTrue(body.contains(reason), "Expected '$reason' in response body: $body")
    }

    @And("no MemberRegistered event is published")
    fun noMemberRegisteredEventPublished() {
        assertEquals(0, testEventListener.getMemberRegisteredEvents().size)
    }
}
