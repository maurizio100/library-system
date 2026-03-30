package com.library.catalog.domain

import com.library.shared.events.DomainEvent

data class BookAdded(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int
) : DomainEvent
