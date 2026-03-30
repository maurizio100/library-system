package com.library.catalog.infra

import org.springframework.data.jpa.repository.JpaRepository

interface BookJpaRepository : JpaRepository<BookJpaEntity, String>
