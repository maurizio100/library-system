package com.library.lending.domain.exception

class NoActiveLoanFoundException(copyBarcode: String) : RuntimeException("No active loan found for this copy")
