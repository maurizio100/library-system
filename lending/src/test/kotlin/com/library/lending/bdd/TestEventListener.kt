package com.library.lending.bdd

import com.library.lending.domain.event.BookReturned
import com.library.lending.domain.event.FeeCharged
import com.library.lending.domain.event.LoanCreated
import com.library.lending.domain.event.MemberRegistered
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestEventListener {

    private val loanCreatedEvents = mutableListOf<LoanCreated>()
    private val memberRegisteredEvents = mutableListOf<MemberRegistered>()
    private val bookReturnedEvents = mutableListOf<BookReturned>()
    private val feeChargedEvents = mutableListOf<FeeCharged>()

    @EventListener
    fun onLoanCreated(event: LoanCreated) {
        loanCreatedEvents.add(event)
    }

    @EventListener
    fun onMemberRegistered(event: MemberRegistered) {
        memberRegisteredEvents.add(event)
    }

    @EventListener
    fun onBookReturned(event: BookReturned) {
        bookReturnedEvents.add(event)
    }

    @EventListener
    fun onFeeCharged(event: FeeCharged) {
        feeChargedEvents.add(event)
    }

    fun getLoanCreatedEvents(): List<LoanCreated> = loanCreatedEvents.toList()

    fun getMemberRegisteredEvents(): List<MemberRegistered> = memberRegisteredEvents.toList()

    fun getBookReturnedEvents(): List<BookReturned> = bookReturnedEvents.toList()

    fun getFeeChargedEvents(): List<FeeCharged> = feeChargedEvents.toList()

    fun clear() {
        loanCreatedEvents.clear()
        memberRegisteredEvents.clear()
        bookReturnedEvents.clear()
        feeChargedEvents.clear()
    }
}
