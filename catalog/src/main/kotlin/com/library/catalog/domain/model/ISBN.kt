package com.library.catalog.domain.model

class ISBN(input: String) {
    val value: String = run {
        require(input.matches(Regex("^[\\d-]+$"))) { "ISBN must contain exactly 13 digits" }
        val digits = input.replace("-", "")
        require(digits.length == 13) { "ISBN must contain exactly 13 digits" }
        digits
    }

    override fun equals(other: Any?) = other is ISBN && value == other.value
    override fun hashCode() = value.hashCode()
    override fun toString() = "ISBN($value)"
}
