@epic:catalog @context:catalog
Feature: Add a Book by Title Search
  As a librarian
  I want to search for a book by title when I don't have its ISBN
  So that I can catalog books quickly even without knowing their ISBN in advance

  Background:
    Given I am on the "Add Book" page

  # ── Happy Path — multiple results ──

  Scenario: Title search returns multiple results and librarian selects one
    Given the catalog does not contain a book with ISBN "9780743273565"
    When I switch to the "Search by title" option
    And I enter title "The Great Gatsby" and request a search
    Then a list of books is displayed containing at least:
      | title            | author             | year | isbn          |
      | The Great Gatsby | F. Scott Fitzgerald | 1925 | 9780743273565 |
    When I select "The Great Gatsby" by "F. Scott Fitzgerald" (1925) from the results
    Then the title "The Great Gatsby", author "F. Scott Fitzgerald", and publication year 1925 are displayed
    When I confirm adding the book
    Then the catalog contains a book with ISBN "9780743273565"
    And the book has title "The Great Gatsby"
    And the book has author "F. Scott Fitzgerald"
    And a success message "Book added to catalog" is displayed

  # ── Happy Path — single result ──

  Scenario: Title search returns exactly one result and it is displayed immediately
    Given the catalog does not contain a book with ISBN "9780062316097"
    When I switch to the "Search by title" option
    And I enter title "Alchemist The" and request a search
    Then the title "The Alchemist", author "Paulo Coelho", and publication year 1988 are displayed
    When I confirm adding the book
    Then the catalog contains a book with ISBN "9780062316097"
    And a success message "Book added to catalog" is displayed

  # ── Variation — librarian edits resolved data ──

  Scenario: Librarian edits data after selecting from title search results
    Given the catalog does not contain a book with ISBN "9780451524935"
    When I switch to the "Search by title" option
    And I enter title "1984" and request a search
    And I select "Nineteen Eighty-Four" by "George Orwell" (1949) from the results
    When I change the title to "1984"
    And I confirm adding the book
    Then the catalog contains a book with ISBN "9780451524935"
    And the book has title "1984"

  # ── Edge Cases & Failures ──

  Scenario: Title search returns no results
    When I switch to the "Search by title" option
    And I enter title "xyzzy nonexistent title 99999" and request a search
    Then a message "No books found for this title" is displayed
    And the confirm button is disabled

  Scenario: Librarian goes back to results after selecting a book
    When I switch to the "Search by title" option
    And I enter title "Dune" and request a search
    Then a list of books is displayed
    When I select "Dune" by "Frank Herbert" (1965) from the results
    And I choose to go back to the results
    Then the list of books for title "Dune" is displayed again

  Scenario: Selected book already exists in the catalog
    Given the catalog contains a book with ISBN "9780441013593" and title "Dune"
    When I switch to the "Search by title" option
    And I enter title "Dune" and request a search
    And I select "Dune" by "Frank Herbert" (1965) from the results
    Then an error message "A book with this ISBN already exists in the catalog" is displayed
    And the confirm button is disabled

  Scenario: Title search service is unavailable
    Given the book search service is unavailable
    When I switch to the "Search by title" option
    And I enter title "Brave New World" and request a search
    Then an error message "Book search service is currently unavailable — please try again later" is displayed

  Scenario: Reject a search with a blank title
    When I switch to the "Search by title" option
    And I enter title "" and request a search
    Then an error message "Please enter a title to search" is displayed
    And no request is made to the search service
