package com.library.lending.bdd

import com.library.lending.domain.event.LoanCreated
import com.library.lending.domain.event.MemberRegistered
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestEventListener {

    private val loanCreatedEvents = mutableListOf<LoanCreated>()
    private val memberRegisteredEvents = mutableListOf<MemberRegistered>()

    @EventListener
    fun onLoanCreated(event: LoanCreated) {
        loanCreatedEvents.add(event)
    }

    @EventListener
    fun onMemberRegistered(event: MemberRegistered) {
        memberRegisteredEvents.add(event)
    }

    fun getLoanCreatedEvents(): List<LoanCreated> = loanCreatedEvents.toList()

    fun getMemberRegisteredEvents(): List<MemberRegistered> = memberRegisteredEvents.toList()

    fun clear() {
        loanCreatedEvents.clear()
        memberRegisteredEvents.clear()
    }
}
