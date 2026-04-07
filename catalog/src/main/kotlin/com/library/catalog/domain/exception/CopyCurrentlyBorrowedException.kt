package com.library.catalog.domain.exception

class CopyCurrentlyBorrowedException : RuntimeException("Cannot remove a copy that is currently borrowed")
