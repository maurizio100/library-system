package com.library.catalog.infra.config

import com.library.catalog.domain.command.AddBookHandler
import com.library.catalog.domain.command.RegisterCopyHandler
import com.library.catalog.domain.port.BookRepository
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
