@epic:catalog @context:catalog
Feature: Remove a Copy from the Catalog
  As a librarian
  I want to remove a physical copy from the catalog
  So that lost or damaged copies no longer appear as available inventory

  Background:
    Given the catalog contains a book with ISBN "9780134685991" and title "Effective Java"
    And a copy with barcode "EJ-001" exists for book with ISBN "9780134685991"

  # ── Happy Path ──

  Scenario: Successfully remove an available copy
    Given copy "EJ-001" has status "Available"
    When I remove copy "EJ-001" from the catalog
    Then the book with ISBN "9780134685991" has 0 copies
    And copy "EJ-001" no longer exists in the catalog

  Scenario: Successfully remove one of several copies
    Given a copy with barcode "EJ-002" exists for book with ISBN "9780134685991"
    And copy "EJ-001" has status "Available"
    When I remove copy "EJ-001" from the catalog
    Then the book with ISBN "9780134685991" has 1 copy
    And copy "EJ-002" still exists with status "Available"

  # ── Variations ──

  Scenario: Book remains in catalog after its last copy is removed
    Given copy "EJ-001" has status "Available"
    When I remove copy "EJ-001" from the catalog
    Then the book with ISBN "9780134685991" still exists in the catalog
    And the book with ISBN "9780134685991" has 0 copies

  # ── Edge Cases & Failures ──

  Scenario: Reject removal of a copy that is currently borrowed
    Given copy "EJ-001" has status "Borrowed"
    When I remove copy "EJ-001" from the catalog
    Then the removal is rejected with reason "Cannot remove a copy that is currently borrowed"
    And copy "EJ-001" still exists with status "Borrowed"

  Scenario: Reject removal of a copy that does not exist
    When I remove copy "UNKNOWN-001" from the catalog
    Then the removal is rejected with reason "Copy not found"

  Scenario: Reject removal with a blank barcode
    When I remove copy "" from the catalog
    Then the removal is rejected with reason "Barcode must not be blank"
