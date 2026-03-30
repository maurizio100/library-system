package com.library.catalog.domain.model

class Copy(
    val barcode: Barcode,
    var status: CopyStatus = CopyStatus.Available
)
