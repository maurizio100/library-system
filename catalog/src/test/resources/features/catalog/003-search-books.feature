@epic:catalog @context:catalog
Feature: Search Books in the Catalog
  As a member
  I want to search for books in the catalog
  So that I can find books I want to borrow and see if copies are available

  Background:
    Given the catalog contains the following books:
      | isbn          | title                | author          | year |
      | 9780134685991 | Effective Java       | Joshua Bloch    | 2018 |
      | 9780596009205 | Head First Design    | Eric Freeman    | 2004 |
      | 9780132350884 | Clean Code           | Robert Martin   | 2008 |
    And the book with ISBN "9780134685991" has 2 copies, 1 Available and 1 Borrowed
    And the book with ISBN "9780596009205" has 1 copy, 1 Available
    And the book with ISBN "9780132350884" has 3 copies, 0 Available

  # ── Happy Path ──

  Scenario: Search by title keyword
    When I search for "Effective"
    Then the search returns 1 result
    And the result contains the book with ISBN "9780134685991" and title "Effective Java"
    And the result shows 1 available copy for ISBN "9780134685991"

  Scenario: Search by author name
    When I search for "Robert Martin"
    Then the search returns 1 result
    And the result contains the book with ISBN "9780132350884" and title "Clean Code"

  Scenario: Search by ISBN
    When I search for "9780596009205"
    Then the search returns 1 result
    And the result contains the book with ISBN "9780596009205" and title "Head First Design"

  # ── Variations ──

  Scenario: Search returns multiple results
    When I search for "Java"
    Then the search returns 1 result
    And the result contains the book with ISBN "9780134685991" and title "Effective Java"

  Scenario: Search is case-insensitive
    When I search for "clean code"
    Then the search returns 1 result
    And the result contains the book with ISBN "9780132350884" and title "Clean Code"

  Scenario: Search result shows zero available copies
    When I search for "Clean Code"
    Then the search returns 1 result
    And the result shows 0 available copies for ISBN "9780132350884"

  # ── Edge Cases & Failures ──

  Scenario: Search with no matching results
    When I search for "Nonexistent Book"
    Then the search returns 0 results

  Scenario: Search with blank query
    When I search for ""
    Then the search is rejected with reason "Search query is required"
