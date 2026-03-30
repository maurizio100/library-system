package com.library.catalog.domain

class DuplicateBarcodeException(barcode: Barcode) : RuntimeException("Barcode already exists")
