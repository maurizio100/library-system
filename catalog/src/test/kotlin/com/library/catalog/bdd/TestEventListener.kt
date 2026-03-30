package com.library.catalog.bdd

import com.library.catalog.domain.BookAdded
import com.library.catalog.domain.CopyRegistered
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestEventListener {

    private val bookAddedEvents = mutableListOf<BookAdded>()
    private val copyRegisteredEvents = mutableListOf<CopyRegistered>()

    @EventListener
    fun onBookAdded(event: BookAdded) {
        bookAddedEvents.add(event)
    }

    @EventListener
    fun onCopyRegistered(event: CopyRegistered) {
        copyRegisteredEvents.add(event)
    }

    fun getBookAddedEvents(): List<BookAdded> = bookAddedEvents.toList()
    fun getCopyRegisteredEvents(): List<CopyRegistered> = copyRegisteredEvents.toList()

    fun clear() {
        bookAddedEvents.clear()
        copyRegisteredEvents.clear()
    }
}
