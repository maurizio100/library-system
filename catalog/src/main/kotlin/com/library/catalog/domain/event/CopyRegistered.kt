package com.library.catalog.domain.event

import com.library.shared.events.DomainEvent

data class CopyRegistered(
    val isbn: String,
    val barcode: String
) : DomainEvent
