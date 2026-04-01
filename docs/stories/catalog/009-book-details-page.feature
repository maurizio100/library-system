@epic:catalog @context:catalog
Feature: Book Details Page
  As a librarian
  I want to view a book's details page
  So that I can see its bibliographic information and the status of all its copies at a glance

  # ── Happy Path ──

  Scenario: View details of a book with a mix of available and borrowed copies
    Given a book with ISBN "9783161484100", title "Clean Code", and author "Robert C. Martin" exists in the catalog
    And the book has a copy with barcode "CC-001" that is Available
    And the book has a copy with barcode "CC-002" that is Borrowed
    When the librarian navigates to the details page for ISBN "9783161484100"
    Then the page displays the title "Clean Code"
    And the page displays the author "Robert C. Martin"
    And the page displays the ISBN "9783161484100"
    And the copies list shows barcode "CC-001" with status "Available"
    And the copies list shows barcode "CC-002" with status "Borrowed"

  # ── Variations ──

  Scenario: View details of a book with no copies registered
    Given a book with ISBN "9780134685991", title "The Pragmatic Programmer", and author "David Thomas" exists in the catalog
    And the book has no copies
    When the librarian navigates to the details page for ISBN "9780134685991"
    Then the page displays the title "The Pragmatic Programmer"
    And the page displays the author "David Thomas"
    And the page displays the ISBN "9780134685991"
    And the copies list shows a message indicating no copies are registered

  Scenario: View details of a book where all copies are available
    Given a book with ISBN "9780201616224", title "The Mythical Man-Month", and author "Frederick P. Brooks" exists in the catalog
    And the book has a copy with barcode "MM-001" that is Available
    And the book has a copy with barcode "MM-002" that is Available
    When the librarian navigates to the details page for ISBN "9780201616224"
    Then the copies list shows barcode "MM-001" with status "Available"
    And the copies list shows barcode "MM-002" with status "Available"

  # ── Edge Cases & Failures ──

  Scenario: Navigate to details page for an ISBN that does not exist
    Given no book with ISBN "9780000000000" exists in the catalog
    When the librarian navigates to the details page for ISBN "9780000000000"
    Then the page displays a message indicating the book was not found
