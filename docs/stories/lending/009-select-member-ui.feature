@epic:lending @context:lending @layer:frontend
Feature: Select a Member from a List
  As a librarian
  I want to pick a Member from a searchable list instead of typing a Member ID
  So that I can process borrowings without needing to look up Member IDs

  Background:
    Given a registered member "Alice" with Member ID "m-001"
    And a registered member "Bob" with Member ID "m-002"
    And the catalog contains a book "Effective Java" with ISBN "9780134685991"
    And the book has an Available copy with barcode "EJ-001"

  # ── Happy Path ──

  Scenario: Borrow a book by selecting a member from the list
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian selects member "Alice" from the member list
    And the librarian selects the copy with barcode "EJ-001" of "Effective Java" from the available copies list
    And the librarian confirms the loan
    Then a success message is displayed showing the Loan details
    And the Loan details include the Member "Alice", book title "Effective Java", copy barcode "EJ-001", and Due Date

  # ── Variations ──

  Scenario: Search for a member by name
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian types "Ali" into the member search field
    Then the member list shows "Alice" as a matching result
    And "Bob" is not shown in the member list

  Scenario: Loan summary shows selected member name before confirmation
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian selects member "Alice" from the member list
    And the librarian selects the copy with barcode "EJ-001" of "Effective Java" from the available copies list
    Then the form shows a summary with Member "Alice" and copy "EJ-001" of "Effective Java" for review before confirming

  # ── Edge Cases & Failures ──

  Scenario: Display error when selected member has reached borrowing limit
    Given "Alice" has reached her Borrowing Limit of 3 active Loans
    And the librarian navigates to the "Borrow a Book" page
    When the librarian selects member "Alice" from the member list
    And the librarian selects the copy with barcode "EJ-001" of "Effective Java" from the available copies list
    And the librarian confirms the loan
    Then an error message "Borrowing limit reached" is displayed
    And no Loan is created

  Scenario: Confirm button is disabled until a member is selected
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian has not selected a member
    Then the confirm button is disabled

  Scenario: No members found for search term
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian types "Zara" into the member search field
    Then the message "No members found" is displayed
