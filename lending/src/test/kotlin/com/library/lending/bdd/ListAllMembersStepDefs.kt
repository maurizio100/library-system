package com.library.lending.bdd

import com.library.lending.infra.persistence.MemberJpaEntity
import com.library.lending.infra.persistence.MemberJpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListAllMembersStepDefs {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var lastResult: MvcResult
    private var membersResponse: List<Map<String, Any>> = emptyList()

    @Before("@epic:lending")
    fun setUp() {
        memberJpaRepository.deleteAll()
    }

    @Given("a registered member {string} with Member ID {string}")
    fun aRegisteredMemberWithMemberId(name: String, memberId: String) {
        memberJpaRepository.save(
            MemberJpaEntity(
                memberId = memberId,
                name = name,
                email = "${name.lowercase()}@example.com",
                borrowingLimit = 3,
                activeLoansCount = 0
            )
        )
    }

    @Given("no Members are registered")
    fun noMembersAreRegistered() {
        memberJpaRepository.deleteAll()
    }

    @When("the librarian requests the list of all Members")
    fun theLibrarianRequestsTheListOfAllMembers() {
        lastResult = mockMvc.perform(get("/api/lending/members")).andReturn()
        assertEquals(200, lastResult.response.status)
        @Suppress("UNCHECKED_CAST")
        membersResponse = objectMapper.readValue(
            lastResult.response.contentAsString,
            List::class.java
        ) as List<Map<String, Any>>
    }

    @Then("the response contains {int} Members")
    fun theResponseContainsMembers(count: Int) {
        assertEquals(count, membersResponse.size)
    }

    @And("the response includes member {string} with Member ID {string}")
    fun theResponseIncludesMemberWithMemberId(name: String, memberId: String) {
        val found = membersResponse.any { it["name"] == name && it["memberId"] == memberId }
        assertTrue(found, "Expected member '$name' with ID '$memberId' in response: $membersResponse")
    }

    @Then("each Member entry contains the name and Member ID")
    fun eachMemberEntryContainsTheNameAndMemberId() {
        assertTrue(membersResponse.isNotEmpty(), "Expected at least one member")
        membersResponse.forEach { member ->
            assertTrue(member.containsKey("name"), "Member entry missing 'name': $member")
            assertTrue(member.containsKey("memberId"), "Member entry missing 'memberId': $member")
        }
    }
}
