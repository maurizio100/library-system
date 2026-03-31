package com.library.lending.api.dto

data class MemberResponse(
    val memberId: String,
    val name: String,
    val email: String,
    val borrowingLimit: Int
)
