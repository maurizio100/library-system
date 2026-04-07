package com.library.catalog.infra.persistence

import com.library.catalog.domain.model.Author
import com.library.catalog.domain.model.Barcode
import com.library.catalog.domain.model.Book
import com.library.catalog.domain.model.Copy
import com.library.catalog.domain.model.CopyStatus
import com.library.catalog.domain.model.ISBN
import com.library.catalog.domain.port.BookRepository
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

    override fun findByBarcode(barcode: Barcode): Book? {
        val copyEntity = copyJpaRepository.findById(barcode.value).orElse(null) ?: return null
        return findByIsbn(ISBN(copyEntity.isbn))
    }

    override fun save(book: Book) {
        bookJpaRepository.save(book.toJpaEntity())
        val existingCopyBarcodes = copyJpaRepository.findByIsbn(book.isbn.value).map { it.barcode }.toSet()
        val domainCopyBarcodes = book.copies.map { it.barcode.value }.toSet()
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
        (existingCopyBarcodes - domainCopyBarcodes).forEach { copyJpaRepository.deleteById(it) }
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
