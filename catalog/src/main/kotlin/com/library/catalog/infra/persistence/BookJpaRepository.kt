package com.library.catalog.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface BookJpaRepository : JpaRepository<BookJpaEntity, String>
