package com.library.catalog.bdd

import com.library.LibraryApplication
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@CucumberContextConfiguration
@SpringBootTest(classes = [LibraryApplication::class])
@AutoConfigureMockMvc
class CucumberSpringConfig
