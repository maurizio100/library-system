@epic:catalog @context:catalog @layer:frontend
Feature: Register a Copy from the Book Details Page
  As a librarian
  I want to register a new Copy directly from a book's details page
  So that I can add physical copies without navigating away from the book I am already viewing

  Background:
    Given a book with ISBN "9780134685991", title "Effective Java", and author "Joshua Bloch" exists in the catalog

  # ── Happy Path ──

  Scenario: Register a copy and see it appear immediately in the copies list
    Given the book has no copies
    And the librarian is on the details page for ISBN "9780134685991"
    When the librarian enters barcode "EJ-001" in the register copy form
    And the librarian confirms the registration
    Then a success message is displayed showing Copy barcode "EJ-001"
    And the copies list shows barcode "EJ-001" with status "Available"

  # ── Variations ──

  Scenario: Register a second copy when the book already has one
    Given the book has a copy with barcode "EJ-001" that is Available
    And the librarian is on the details page for ISBN "9780134685991"
    When the librarian enters barcode "EJ-002" in the register copy form
    And the librarian confirms the registration
    Then a success message is displayed showing Copy barcode "EJ-002"
    And the copies list shows barcode "EJ-001" with status "Available"
    And the copies list shows barcode "EJ-002" with status "Available"

  # ── Edge Cases & Failures ──

  Scenario: Display error when barcode already exists
    Given the book has a copy with barcode "EJ-001" that is Available
    And the librarian is on the details page for ISBN "9780134685991"
    When the librarian enters barcode "EJ-001" in the register copy form
    And the librarian confirms the registration
    Then an error message "Barcode already exists" is displayed
    And the copies list still shows exactly 1 copy

  Scenario: Prevent submission with an empty barcode
    Given the librarian is on the details page for ISBN "9780134685991"
    When the librarian leaves the barcode field empty
    Then the confirm button is disabled
