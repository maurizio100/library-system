package com.library.catalog.infra.persistence

import com.library.catalog.domain.port.BookSearchPort
import com.library.catalog.domain.port.BookSearchResult
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class BookSearchAdapter(
    private val entityManager: EntityManager
) : BookSearchPort {

    override fun search(query: String): List<BookSearchResult> {
        val lowerQuery = "%${query.lowercase()}%"

        val sql = """
            SELECT b.isbn, b.title, b.publication_year
            FROM books b
            LEFT JOIN book_jpa_entity_authors a ON b.isbn = a.book_jpa_entity_isbn
            WHERE LOWER(b.isbn) LIKE :query
               OR LOWER(b.title) LIKE :query
               OR LOWER(a.authors) LIKE :query
            GROUP BY b.isbn, b.title, b.publication_year
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(sql)
            .setParameter("query", lowerQuery)
            .resultList as List<Array<Any>>

        return rows.map { row -> toBookSearchResult(row) }
    }

    override fun findAll(): List<BookSearchResult> {
        val sql = """
            SELECT b.isbn, b.title, b.publication_year
            FROM books b
            ORDER BY b.title
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(sql)
            .resultList as List<Array<Any>>

        return rows.map { row -> toBookSearchResult(row) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun toBookSearchResult(row: Array<Any>): BookSearchResult {
        val isbn = row[0] as String
        val title = row[1] as String
        val year = row[2] as Int

        val authors = entityManager.createNativeQuery(
            "SELECT authors FROM book_jpa_entity_authors WHERE book_jpa_entity_isbn = :isbn"
        ).setParameter("isbn", isbn).resultList as List<String>

        val totalCopies = (entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM copies WHERE isbn = :isbn"
        ).setParameter("isbn", isbn).singleResult as Number).toInt()

        val availableCopies = (entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM copies WHERE isbn = :isbn AND status = 'Available'"
        ).setParameter("isbn", isbn).singleResult as Number).toInt()

        return BookSearchResult(
            isbn = isbn,
            title = title,
            authors = authors,
            publicationYear = year,
            totalCopies = totalCopies,
            availableCopies = availableCopies
        )
    }
}
