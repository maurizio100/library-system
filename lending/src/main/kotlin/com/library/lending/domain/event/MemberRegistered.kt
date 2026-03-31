package com.library.lending.domain.event

import com.library.shared.events.DomainEvent

data class MemberRegistered(
    val memberId: String,
    val name: String,
    val email: String
) : DomainEvent
