package com.library.lending.infra.adapter

import com.library.catalog.infra.persistence.CopyJpaRepository
import com.library.lending.domain.port.CopyAvailabilityPort
import org.springframework.stereotype.Component

@Component
class CopyAvailabilityAdapter(
    private val copyJpaRepository: CopyJpaRepository
) : CopyAvailabilityPort {

    override fun isCopyAvailable(copyBarcode: String): Boolean {
        val copy = copyJpaRepository.findById(copyBarcode).orElse(null) ?: return false
        return copy.status == "Available"
    }

    override fun markCopyAsBorrowed(copyBarcode: String) {
        val copy = copyJpaRepository.findById(copyBarcode).orElse(null) ?: return
        val updatedCopy = com.library.catalog.infra.persistence.CopyJpaEntity(
            barcode = copy.barcode,
            isbn = copy.isbn,
            status = "Borrowed"
        )
        copyJpaRepository.save(updatedCopy)
    }
}
