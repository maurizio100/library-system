package com.library.catalog.infra

import com.library.catalog.domain.AddBookHandler
import com.library.catalog.domain.BookRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CatalogConfiguration {

    @Bean
    fun addBookHandler(bookRepository: BookRepository): AddBookHandler {
        return AddBookHandler(bookRepository)
    }
}
