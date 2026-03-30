package com.library.lending.domain.exception

class CopyNotAvailableException(copyBarcode: String) : RuntimeException("Copy is not available")
