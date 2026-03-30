package com.library.catalog.infra

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "books")
class BookJpaEntity(
    @Id
    val isbn: String = "",
    val title: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    val authors: MutableList<String> = mutableListOf(),
    val publicationYear: Int = 0
)
