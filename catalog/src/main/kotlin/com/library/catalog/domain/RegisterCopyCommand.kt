package com.library.catalog.domain

data class RegisterCopyCommand(
    val isbn: ISBN,
    val barcode: Barcode
)
