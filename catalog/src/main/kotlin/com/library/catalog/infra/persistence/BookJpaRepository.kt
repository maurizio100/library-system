package com.library.catalog.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookJpaRepository : JpaRepository<BookJpaEntity, String> {

    @Query("""
        SELECT DISTINCT b FROM BookJpaEntity b LEFT JOIN b.authors a
        WHERE LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(a) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun search(query: String): List<BookJpaEntity>

    fun findAllByOrderByTitleAsc(): List<BookJpaEntity>
}
