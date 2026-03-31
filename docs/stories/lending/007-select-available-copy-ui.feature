@epic:lending @context:lending @layer:frontend
Feature: Select an Available Copy from a List
  As a librarian
  I want to pick an Available Copy from a searchable list instead of typing a barcode
  So that I can process borrowings without needing to know barcodes by heart

  Background:
    Given the catalog contains a book "Effective Java" with ISBN "9780134685991"
    And the book has an Available copy with barcode "EJ-001"
    And the catalog contains a book "Clean Code" with ISBN "9780132350884"
    And the book has an Available copy with barcode "CC-001"
    And a registered member "Alice" with Member ID "m-001"

  # ── Happy Path ──

  Scenario: Borrow a book by selecting an available copy from the list
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001"
    And the librarian selects the copy with barcode "EJ-001" of "Effective Java" from the available copies list
    And the librarian confirms the loan
    Then a success message is displayed showing the Loan details
    And the Loan details include the Member ID "m-001", book title "Effective Java", copy barcode "EJ-001", and Due Date

  # ── Variations ──

  Scenario: Search for an available copy by book title
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian types "Clean" into the copy search field
    Then the available copies list shows "Clean Code" with barcode "CC-001"
    And "Effective Java" is not shown in the available copies list

  Scenario: Only available copies are shown in the list
    Given the copy "EJ-001" is already Borrowed
    And the librarian navigates to the "Borrow a Book" page
    When the librarian views the available copies list
    Then the copy with barcode "EJ-001" is not shown
    And the copy with barcode "CC-001" is shown

  Scenario: Loan summary shows selected copy details before confirmation
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001"
    And the librarian selects the copy with barcode "EJ-001" of "Effective Java" from the available copies list
    Then the form shows a summary with Member ID "m-001" and copy "EJ-001" of "Effective Java" for review before confirming

  # ── Edge Cases & Failures ──

  Scenario: No available copies message when all copies are borrowed
    Given the copy "EJ-001" is already Borrowed
    And the copy "CC-001" is already Borrowed
    And the librarian navigates to the "Borrow a Book" page
    When the librarian views the available copies list
    Then the message "No available copies" is displayed

  Scenario: Confirm button is disabled until a copy is selected
    Given the librarian navigates to the "Borrow a Book" page
    When the librarian enters Member ID "m-001"
    But has not selected a copy
    Then the confirm button is disabled
