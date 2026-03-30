@epic:catalog @context:catalog
Feature: Register a Copy of a Book
  As a librarian
  I want to register physical copies of books
  So that they can be tracked and lent out to members

  Background:
    Given the catalog contains a book with ISBN "9780134685991" and title "Effective Java"

  # ── Happy Path ──

  Scenario: Successfully register a new copy
    When I register a copy with barcode "EJ-001" for book with ISBN "9780134685991"
    Then the book with ISBN "9780134685991" has 1 copy
    And the copy with barcode "EJ-001" has status "Available"
    And a CopyRegistered event is published with barcode "EJ-001" and ISBN "9780134685991"

  Scenario: Register multiple copies of the same book
    Given a copy with barcode "EJ-001" exists for book with ISBN "9780134685991"
    When I register a copy with barcode "EJ-002" for book with ISBN "9780134685991"
    Then the book with ISBN "9780134685991" has 2 copies

  # ── Edge Cases & Failures ──

  Scenario: Reject duplicate barcode
    Given a copy with barcode "EJ-001" exists for book with ISBN "9780134685991"
    When I register a copy with barcode "EJ-001" for book with ISBN "9780134685991"
    Then the copy is rejected with reason "Barcode already exists"

  Scenario: Reject copy for nonexistent book
    When I register a copy with barcode "XX-001" for book with ISBN "0000000000000"
    Then the copy is rejected with reason "Book not found"
