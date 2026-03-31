@epic:catalog @context:catalog @layer:frontend
Feature: Register a Copy via the UI
  As a librarian
  I want to register a new Copy by selecting a Book from the catalog
  So that I can add physical copies without having to type the ISBN manually

  Background:
    Given the catalog contains the following books:
      | isbn          | title             | author          | year |
      | 9780134685991 | Effective Java    | Joshua Bloch    | 2018 |
      | 9780132350884 | Clean Code        | Robert Martin   | 2008 |

  # ── Happy Path ──

  Scenario: Register a copy by selecting a book from the catalog list
    Given the librarian navigates to the "Books" page
    When the librarian selects the book "Effective Java" from the list
    And the librarian chooses to register a new Copy
    And the librarian enters barcode "EJ-001"
    And the librarian confirms the registration
    Then a success message is displayed showing Copy barcode "EJ-001" for book "Effective Java"
    And the book "Effective Java" shows 1 copy in the catalog

  Scenario: Register a copy by selecting a book from search results
    Given the librarian navigates to the "Books" page
    When the librarian searches for "Clean Code"
    And the librarian selects the book "Clean Code" from the results
    And the librarian chooses to register a new Copy
    And the librarian enters barcode "CC-001"
    And the librarian confirms the registration
    Then a success message is displayed showing Copy barcode "CC-001" for book "Clean Code"

  # ── Variations ──

  Scenario: Register multiple copies one at a time
    Given the librarian navigates to the "Books" page
    And the librarian selects the book "Effective Java" from the list
    When the librarian chooses to register a new Copy
    And the librarian enters barcode "EJ-001"
    And the librarian confirms the registration
    Then a success message is displayed showing Copy barcode "EJ-001" for book "Effective Java"
    When the librarian chooses to register a new Copy
    And the librarian enters barcode "EJ-002"
    And the librarian confirms the registration
    Then a success message is displayed showing Copy barcode "EJ-002" for book "Effective Java"
    And the book "Effective Java" shows 2 copies in the catalog

  # ── Edge Cases & Failures ──

  Scenario: Display error when barcode already exists
    Given a Copy with barcode "EJ-001" already exists in the catalog
    And the librarian navigates to the "Books" page
    And the librarian selects the book "Effective Java" from the list
    When the librarian chooses to register a new Copy
    And the librarian enters barcode "EJ-001"
    And the librarian confirms the registration
    Then an error message "Barcode already exists" is displayed
    And no new Copy is registered

  Scenario: Prevent submission with empty barcode
    Given the librarian navigates to the "Books" page
    And the librarian selects the book "Effective Java" from the list
    When the librarian chooses to register a new Copy
    And the librarian leaves the barcode field empty
    Then the confirm button is disabled
