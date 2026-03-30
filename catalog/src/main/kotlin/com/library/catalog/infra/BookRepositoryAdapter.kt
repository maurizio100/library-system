package com.library.catalog.infra

import com.library.catalog.domain.Author
import com.library.catalog.domain.Book
import com.library.catalog.domain.BookRepository
import com.library.catalog.domain.ISBN
import org.springframework.stereotype.Repository

@Repository
class BookRepositoryAdapter(
    private val jpaRepository: BookJpaRepository
) : BookRepository {

    override fun findByIsbn(isbn: ISBN): Book? {
        return jpaRepository.findById(isbn.value).orElse(null)?.toDomain()
    }

    override fun save(book: Book) {
        jpaRepository.save(book.toJpaEntity())
    }

    override fun existsByIsbn(isbn: ISBN): Boolean {
        return jpaRepository.existsById(isbn.value)
    }

    private fun BookJpaEntity.toDomain(): Book {
        return Book(
            isbn = ISBN(isbn),
            title = title,
            authors = authors.map { Author(it) },
            publicationYear = publicationYear
        )
    }

    private fun Book.toJpaEntity(): BookJpaEntity {
        return BookJpaEntity(
            isbn = isbn.value,
            title = title,
            authors = authors.map { it.name }.toMutableList(),
            publicationYear = publicationYear
        )
    }
}
