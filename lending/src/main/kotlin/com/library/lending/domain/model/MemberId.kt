package com.library.lending.domain.model

data class MemberId(val value: String) {
    init {
        require(value.isNotBlank()) { "Member ID is required" }
    }
}
