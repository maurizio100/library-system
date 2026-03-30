package com.library.catalog.infra

import com.library.catalog.domain.AddBookHandler
import com.library.catalog.domain.BookRepository
import com.library.catalog.domain.RegisterCopyHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CatalogConfiguration {

    @Bean
    fun addBookHandler(bookRepository: BookRepository): AddBookHandler {
        return AddBookHandler(bookRepository)
    }

    @Bean
    fun registerCopyHandler(bookRepository: BookRepository): RegisterCopyHandler {
        return RegisterCopyHandler(bookRepository)
    }
}
