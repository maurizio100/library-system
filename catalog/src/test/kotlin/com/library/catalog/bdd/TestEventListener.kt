package com.library.catalog.bdd

import com.library.catalog.domain.BookAdded
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestEventListener {

    private val capturedEvents = mutableListOf<BookAdded>()

    @EventListener
    fun onBookAdded(event: BookAdded) {
        capturedEvents.add(event)
    }

    fun getBookAddedEvents(): List<BookAdded> = capturedEvents.toList()

    fun clear() {
        capturedEvents.clear()
    }
}
