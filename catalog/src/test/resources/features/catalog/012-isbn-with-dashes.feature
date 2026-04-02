@epic:catalog @context:catalog
Feature: Accept ISBN with dashes
  As a librarian
  I want to enter an ISBN using the hyphenated format (e.g. 978-3-16-148410-0)
  So that I can use the ISBN exactly as printed on the book without manual reformatting

  # ── Happy Path ──

  Scenario: Add a book using a hyphenated ISBN
    Given no book with ISBN "9783161484100" exists in the catalog
    When the librarian adds a book with ISBN "978-3-16-148410-0", title "The Critique of Pure Reason", author "Immanuel Kant", and publication year 1781
    Then the book is added to the catalog with the normalised ISBN "9783161484100"

  Scenario: Look up a book using a hyphenated ISBN
    Given a book with ISBN "9783161484100" exists in the catalog
    When the librarian looks up the book by ISBN "978-3-16-148410-0"
    Then the book with title "The Critique of Pure Reason" is returned

  # ── Edge Cases & Failures ──

  Scenario: Reject a hyphenated ISBN whose digits total fewer than 13
    Given no book exists in the catalog
    When the librarian adds a book with ISBN "978-3-16-14841-0", title "Some Book", author "Some Author", and publication year 2020
    Then the system rejects the request with the message "ISBN must contain exactly 13 digits"

  Scenario: Reject a hyphenated ISBN whose digits total more than 13
    Given no book exists in the catalog
    When the librarian adds a book with ISBN "978-3-16-1484100-0", title "Some Book", author "Some Author", and publication year 2020
    Then the system rejects the request with the message "ISBN must contain exactly 13 digits"

  Scenario: Reject an ISBN that contains characters other than digits and dashes
    Given no book exists in the catalog
    When the librarian adds a book with ISBN "978-3-16-14841O-0", title "Some Book", author "Some Author", and publication year 2020
    Then the system rejects the request with the message "ISBN must contain exactly 13 digits"
