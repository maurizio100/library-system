package com.library.catalog.infra.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "copies")
class CopyJpaEntity(
    @Id
    val barcode: String = "",
    val isbn: String = "",
    val status: String = "Available"
)
