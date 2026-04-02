@epic:catalog @context:catalog
Feature: Add a Book to the Catalog
  As a librarian
  I want to add new books to the catalog
  So that members can discover and borrow them

  # ── Happy Path ──

  Scenario: Successfully add a new book
    Given the catalog does not contain a book with ISBN "9780134685991"
    When I add a book with ISBN "9780134685991", title "Effective Java", author "Joshua Bloch", and publication year 2018
    Then the catalog contains a book with ISBN "9780134685991"
    And the book has title "Effective Java"
    And the book has author "Joshua Bloch"
    And a BookAdded event is published with ISBN "9780134685991"

  # ── Edge Cases & Failures ──

  Scenario: Reject duplicate ISBN
    Given the catalog contains a book with ISBN "9780134685991" and title "Effective Java"
    When I add a book with ISBN "9780134685991", title "Effective Java 2", author "Someone Else", and publication year 2024
    Then the book is rejected with reason "ISBN already exists"
    And the catalog still contains the original book with title "Effective Java"

  Scenario: Reject invalid ISBN
    When I add a book with ISBN "123", title "Bad Book", author "Nobody", and publication year 2024
    Then the book is rejected with reason "ISBN must contain exactly 13 digits (got 3)"

  Scenario: Reject book without title
    When I add a book with ISBN "9780134685991", title "", author "Joshua Bloch", and publication year 2018
    Then the book is rejected with reason "Title is required"

  Scenario: Reject book without author
    When I add a book with ISBN "9780134685991", title "Effective Java", author "", and publication year 2018
    Then the book is rejected with reason "Author is required"
