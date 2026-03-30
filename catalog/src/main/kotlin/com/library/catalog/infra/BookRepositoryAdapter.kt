package com.library.catalog.infra

import com.library.catalog.domain.Author
import com.library.catalog.domain.Barcode
import com.library.catalog.domain.Book
import com.library.catalog.domain.BookRepository
import com.library.catalog.domain.Copy
import com.library.catalog.domain.CopyStatus
import com.library.catalog.domain.ISBN
import org.springframework.stereotype.Repository

@Repository
class BookRepositoryAdapter(
    private val bookJpaRepository: BookJpaRepository,
    private val copyJpaRepository: CopyJpaRepository
) : BookRepository {

    override fun findByIsbn(isbn: ISBN): Book? {
        val bookEntity = bookJpaRepository.findById(isbn.value).orElse(null) ?: return null
        val copyEntities = copyJpaRepository.findByIsbn(isbn.value)
        return bookEntity.toDomain(copyEntities)
    }

    override fun save(book: Book) {
        bookJpaRepository.save(book.toJpaEntity())
        val existingCopyBarcodes = copyJpaRepository.findByIsbn(book.isbn.value).map { it.barcode }.toSet()
        book.copies
            .filter { it.barcode.value !in existingCopyBarcodes }
            .forEach { copy ->
                copyJpaRepository.save(
                    CopyJpaEntity(
                        barcode = copy.barcode.value,
                        isbn = book.isbn.value,
                        status = copy.status.name
                    )
                )
            }
    }

    override fun existsByIsbn(isbn: ISBN): Boolean {
        return bookJpaRepository.existsById(isbn.value)
    }

    private fun BookJpaEntity.toDomain(copyEntities: List<CopyJpaEntity>): Book {
        val copies = copyEntities.map { copyEntity ->
            Copy(
                barcode = Barcode(copyEntity.barcode),
                status = CopyStatus.valueOf(copyEntity.status)
            )
        }.toMutableList()
        return Book(
            isbn = ISBN(isbn),
            title = title,
            authors = authors.map { Author(it) },
            publicationYear = publicationYear,
            _copies = copies
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
