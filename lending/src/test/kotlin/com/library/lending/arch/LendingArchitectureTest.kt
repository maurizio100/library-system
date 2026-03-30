package com.library.lending.arch

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class LendingArchitectureTest {

    private val classes = ClassFileImporter().importPackages("com.library.lending")

    @Test
    fun `domain layer must not depend on API layer`() {
        noClasses()
            .that().resideInAPackage("com.library.lending.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.library.lending.api..")
            .check(classes)
    }

    @Test
    fun `domain layer must not depend on infra layer`() {
        noClasses()
            .that().resideInAPackage("com.library.lending.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.library.lending.infra..")
            .check(classes)
    }

    @Test
    fun `domain layer must not depend on Spring framework`() {
        noClasses()
            .that().resideInAPackage("com.library.lending.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .check(classes)
    }

    @Test
    fun `lending domain must not depend on catalog domain`() {
        noClasses()
            .that().resideInAPackage("com.library.lending.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.library.catalog..")
            .check(classes)
    }
}
