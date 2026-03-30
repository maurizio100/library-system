package com.library.catalog.arch

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class CatalogArchitectureTest {

    private val classes = ClassFileImporter().importPackages("com.library.catalog")

    @Test
    fun `domain layer must not depend on API layer`() {
        noClasses()
            .that().resideInAPackage("com.library.catalog.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.library.catalog.api..")
            .check(classes)
    }

    @Test
    fun `domain layer must not depend on infra layer`() {
        noClasses()
            .that().resideInAPackage("com.library.catalog.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.library.catalog.infra..")
            .check(classes)
    }

    @Test
    fun `domain layer must not depend on Spring framework`() {
        noClasses()
            .that().resideInAPackage("com.library.catalog.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .check(classes)
    }

    @Test
    fun `catalog must not depend on lending`() {
        noClasses()
            .that().resideInAPackage("com.library.catalog..")
            .should().dependOnClassesThat().resideInAPackage("com.library.lending..")
            .check(classes)
    }
}
