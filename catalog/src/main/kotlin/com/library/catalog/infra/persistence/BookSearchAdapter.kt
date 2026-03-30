package com.library.catalog.infra.persistence

import com.library.catalog.domain.port.BookSearchPort
import com.library.catalog.domain.port.BookSearchResult
import org.springframework.stereotype.Component

@Component
class BookSearchAdapter(
    private val bookJpaRepository: BookJpaRepository,
    private val copyJpaRepository: CopyJpaRepository
) : BookSearchPort {

    override fun search(query: String): List<BookSearchResult> {
        return bookJpaRepository.search(query).map { toBookSearchResult(it) }
    }

    override fun findAll(): List<BookSearchResult> {
        return bookJpaRepository.findAllByOrderByTitleAsc().map { toBookSearchResult(it) }
    }

    private fun toBookSearchResult(entity: BookJpaEntity): BookSearchResult {
        return BookSearchResult(
            isbn = entity.isbn,
            title = entity.title,
            authors = entity.authors.toList(),
            publicationYear = entity.publicationYear,
            totalCopies = copyJpaRepository.countByIsbn(entity.isbn),
            availableCopies = copyJpaRepository.countByIsbnAndStatus(entity.isbn, "Available")
        )
    }
}
