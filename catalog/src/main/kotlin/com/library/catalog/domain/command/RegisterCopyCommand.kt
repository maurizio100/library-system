package com.library.catalog.domain.command

import com.library.catalog.domain.model.Barcode
import com.library.catalog.domain.model.ISBN

data class RegisterCopyCommand(
    val isbn: ISBN,
    val barcode: Barcode
)
