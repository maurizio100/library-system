package com.library.catalog.domain

class Copy(
    val barcode: Barcode,
    var status: CopyStatus = CopyStatus.Available
)
