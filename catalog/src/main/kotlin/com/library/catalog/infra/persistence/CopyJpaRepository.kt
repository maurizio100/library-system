package com.library.catalog.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CopyJpaRepository : JpaRepository<CopyJpaEntity, String> {
    fun findByIsbn(isbn: String): List<CopyJpaEntity>
    fun existsByBarcode(barcode: String): Boolean
    fun countByIsbn(isbn: String): Int
    fun countByIsbnAndStatus(isbn: String, status: String): Int
}
