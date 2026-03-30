package com.library.lending.domain.model

import java.util.UUID

data class LoanId(val value: UUID = UUID.randomUUID())
