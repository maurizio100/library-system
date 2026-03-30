package com.library.catalog.domain.exception

import com.library.catalog.domain.model.Barcode

class DuplicateBarcodeException(barcode: Barcode) : RuntimeException("Barcode already exists")
