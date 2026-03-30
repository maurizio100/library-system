package com.library.lending.bdd

import com.library.lending.domain.event.LoanCreated
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestEventListener {

    private val loanCreatedEvents = mutableListOf<LoanCreated>()

    @EventListener
    fun onLoanCreated(event: LoanCreated) {
        loanCreatedEvents.add(event)
    }

    fun getLoanCreatedEvents(): List<LoanCreated> = loanCreatedEvents.toList()

    fun clear() {
        loanCreatedEvents.clear()
    }
}
