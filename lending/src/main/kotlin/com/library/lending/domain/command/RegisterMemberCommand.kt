package com.library.lending.domain.command

data class RegisterMemberCommand(
    val name: String,
    val email: String
)
