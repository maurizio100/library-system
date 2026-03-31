package com.library.lending.domain.port

interface CopyAvailabilityPort {
    fun isCopyAvailable(copyBarcode: String): Boolean
    fun markCopyAsBorrowed(copyBarcode: String)
    fun markCopyAsAvailable(copyBarcode: String)
}
