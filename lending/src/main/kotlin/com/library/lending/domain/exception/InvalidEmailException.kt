package com.library.lending.domain.exception

class InvalidEmailException(email: String) :
    RuntimeException("Invalid email address")
