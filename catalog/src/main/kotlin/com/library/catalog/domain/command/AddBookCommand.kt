package com.library.catalog.domain.command

import com.library.catalog.domain.model.Author
import com.library.catalog.domain.model.ISBN

data class AddBookCommand(
    val isbn: ISBN,
    val title: String,
    val authors: List<Author>,
    val publicationYear: Int
)
