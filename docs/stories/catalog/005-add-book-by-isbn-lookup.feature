@epic:catalog @context:catalog
Feature: Add a Book by ISBN Lookup
  As a librarian
  I want to add a book by entering only its ISBN and having the details resolved automatically
  So that I can catalog new books quickly without manually typing titles and authors

  Background:
    Given I am on the "Add Book" page

  # ── Happy Path ──

  Scenario: Successfully look up and add a book by ISBN
    Given the catalog does not contain a book with ISBN "9780134685991"
    When I enter ISBN "9780134685991" and request a lookup
    Then the title "Effective Java", author "Joshua Bloch", and publication year 2018 are displayed
    When I confirm adding the book
    Then the catalog contains a book with ISBN "9780134685991"
    And the book has title "Effective Java"
    And the book has author "Joshua Bloch"
    And a success message "Book added to catalog" is displayed

  Scenario: Librarian edits resolved data before adding
    Given the catalog does not contain a book with ISBN "9780596007126"
    When I enter ISBN "9780596007126" and request a lookup
    Then the title "Head First Design Patterns", author "Eric Freeman", and publication year 2004 are displayed
    When I change the title to "Head First Design Patterns (Revised)"
    And I confirm adding the book
    Then the catalog contains a book with ISBN "9780596007126"
    And the book has title "Head First Design Patterns (Revised)"

  # ── Edge Cases & Failures ──

  Scenario: ISBN not found in the lookup service
    When I enter ISBN "9780000000000" and request a lookup
    Then an error message "No book found for this ISBN" is displayed
    And the confirm button is disabled

  Scenario: ISBN already exists in the catalog
    Given the catalog contains a book with ISBN "9780134685991" and title "Effective Java"
    When I enter ISBN "9780134685991" and request a lookup
    Then an error message "A book with this ISBN already exists in the catalog" is displayed
    And the confirm button is disabled

  Scenario: Lookup service is unavailable
    Given the ISBN lookup service is unavailable
    When I enter ISBN "9780134685991" and request a lookup
    Then an error message "ISBN lookup service is currently unavailable — please try again later" is displayed
    And the confirm button is disabled

  Scenario: Reject invalid ISBN format
    When I enter ISBN "123" and request a lookup
    Then an error message "Invalid ISBN format" is displayed
    And no request is made to the lookup service

  Scenario: Lookup returns incomplete data
    Given the ISBN lookup service returns no publication year for ISBN "9781234567897"
    When I enter ISBN "9781234567897" and request a lookup
    Then the resolved title and author are displayed
    And the publication year field is empty and editable
    When I enter publication year 2020
    And I confirm adding the book
    Then the catalog contains a book with ISBN "9781234567897" and publication year 2020
