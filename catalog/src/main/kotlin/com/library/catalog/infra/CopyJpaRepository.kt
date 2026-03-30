package com.library.catalog.infra

import org.springframework.data.jpa.repository.JpaRepository

interface CopyJpaRepository : JpaRepository<CopyJpaEntity, String> {
    fun findByIsbn(isbn: String): List<CopyJpaEntity>
    fun existsByBarcode(barcode: String): Boolean
}
