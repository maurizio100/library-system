@epic:lending @context:lending @layer:frontend
Feature: Borrow a Book via the UI
  As a librarian
  I want to create a Loan through the library's web interface
  So that I can process book borrowings without using the API directly

  Background:
    Given the catalog contains a book "Effective Java" with ISBN "9780134685991"
    And the book has an Available copy with barcode "EJ-001"
    And a registered member "Alice" with Member ID "m-001"

  # ── Happy Path ──

  Scenario: Successfully borrow a book through the form
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001" and copy barcode "EJ-001"
    And the librarian confirms the loan
    Then a success message is displayed showing the Loan details
    And the Loan details include the Member ID "m-001", copy barcode "EJ-001", and Due Date

  # ── Variations ──

  Scenario: Borrow page shows loan summary before confirmation
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001" and copy barcode "EJ-001"
    Then the form shows the Member ID and copy barcode for review before confirming

  # ── Edge Cases & Failures ──

  Scenario: Display error when member has reached borrowing limit
    Given "Alice" has reached her Borrowing Limit of 3 active Loans
    And the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001" and copy barcode "EJ-001"
    And the librarian confirms the loan
    Then an error message "Borrowing limit reached" is displayed
    And no Loan is created

  Scenario: Display error when copy is not available
    Given the copy "EJ-001" is already Borrowed
    And the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001" and copy barcode "EJ-001"
    And the librarian confirms the loan
    Then an error message "Copy is not available" is displayed

  Scenario: Display error for unknown member
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-999" and copy barcode "EJ-001"
    And the librarian confirms the loan
    Then an error message "Member not found" is displayed

  Scenario: Prevent submission with empty fields
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian leaves the Member ID and copy barcode fields empty
    Then the confirm button is disabled
