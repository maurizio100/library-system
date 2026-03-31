package com.library.lending.domain.command

import com.library.lending.domain.exception.DuplicateEmailException
import com.library.lending.domain.exception.InvalidEmailException
import com.library.lending.domain.exception.MemberNameRequiredException
import com.library.lending.domain.model.Member
import com.library.lending.domain.model.MemberId
import com.library.lending.domain.port.MemberRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RegisterMemberHandlerTest {

    private val members = mutableMapOf<String, Member>()

    private val memberRepository = object : MemberRepository {
        override fun findById(memberId: MemberId): Member? = members[memberId.value]
        override fun findByEmail(email: String): Member? = members.values.find { it.email == email }
        override fun findAll(): List<Member> = members.values.toList()
        override fun save(member: Member) {
            members[member.memberId.value] = member
        }
    }

    private val handler = RegisterMemberHandler(memberRepository)

    @Test
    fun `successfully registers a new member`() {
        val event = handler.handle(RegisterMemberCommand("Alice Thompson", "alice@example.com"))

        assertNotNull(event.memberId)
        assertEquals("Alice Thompson", event.name)
        assertEquals("alice@example.com", event.email)
        assertEquals(1, members.size)

        val saved = members.values.first()
        assertEquals("Alice Thompson", saved.name)
        assertEquals("alice@example.com", saved.email)
        assertEquals(3, saved.borrowingLimit)
    }

    @Test
    fun `rejects duplicate email`() {
        handler.handle(RegisterMemberCommand("Alice", "alice@example.com"))

        val ex = assertThrows<DuplicateEmailException> {
            handler.handle(RegisterMemberCommand("Alice Duplicate", "alice@example.com"))
        }
        assertEquals("A member with this email already exists", ex.message)
    }

    @Test
    fun `rejects invalid email`() {
        val ex = assertThrows<InvalidEmailException> {
            handler.handle(RegisterMemberCommand("Charlie", "not-an-email"))
        }
        assertEquals("Invalid email address", ex.message)
    }

    @Test
    fun `rejects empty name`() {
        val ex = assertThrows<MemberNameRequiredException> {
            handler.handle(RegisterMemberCommand("", "empty@example.com"))
        }
        assertEquals("Member name is required", ex.message)
    }

    @Test
    fun `rejects blank name`() {
        assertThrows<MemberNameRequiredException> {
            handler.handle(RegisterMemberCommand("   ", "blank@example.com"))
        }
    }
}
