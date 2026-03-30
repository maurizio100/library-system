@epic:catalog @context:catalog
Feature: List All Books in the Catalog
  As a librarian
  I want to list all books in the catalog
  So that I can get a complete overview of the library's collection

  # ── Happy Path ──

  Scenario: List all books when the catalog has multiple books
    Given the catalog contains the following books:
      | isbn          | title                | author          | year |
      | 9780134685991 | Effective Java       | Joshua Bloch    | 2018 |
      | 9780596009205 | Head First Design    | Eric Freeman    | 2004 |
      | 9780132350884 | Clean Code           | Robert Martin   | 2008 |
    When I list all books
    Then I receive 3 books
    And the list contains a book with ISBN "9780134685991" and title "Effective Java"
    And the list contains a book with ISBN "9780596009205" and title "Head First Design"
    And the list contains a book with ISBN "9780132350884" and title "Clean Code"

  Scenario: Listed books include their copy availability
    Given the catalog contains the following books:
      | isbn          | title                | author          | year |
      | 9780134685991 | Effective Java       | Joshua Bloch    | 2018 |
      | 9780132350884 | Clean Code           | Robert Martin   | 2008 |
    And the book with ISBN "9780134685991" has 2 copies, 1 Available and 1 Borrowed
    And the book with ISBN "9780132350884" has 3 copies, 0 Available
    When I list all books
    Then the list shows 1 available copy out of 2 total copies for ISBN "9780134685991"
    And the list shows 0 available copies out of 3 total copies for ISBN "9780132350884"

  # ── Edge Cases ──

  Scenario: List books when the catalog is empty
    Given the catalog contains no books
    When I list all books
    Then I receive 0 books
