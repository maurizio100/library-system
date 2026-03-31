package com.library.lending.domain.exception

class DuplicateEmailException(email: String) :
    RuntimeException("A member with this email already exists")
