@epic:lending @context:lending
Feature: Borrow a Book
  As a member
  I want to borrow a book from the library
  So that I can read it at home

  Background:
    Given a member "Alice" with member ID "m-001" and borrowing limit 3
    And the catalog contains a book with ISBN "9780134685991" and an available copy with barcode "EJ-001"

  # ── Happy Path ──

  Scenario: Successfully borrow a book
    Given "Alice" has 0 active loans
    When "Alice" borrows the copy with barcode "EJ-001"
    Then a loan is created for member "m-001" and copy "EJ-001"
    And the loan due date is 14 days from today
    And a LoanCreated event is published with member "m-001" and copy "EJ-001"
    And the copy "EJ-001" is marked as Borrowed in the catalog

  # ── Variations ──

  Scenario: Borrow when member has existing loans below limit
    Given "Alice" has 2 active loans
    When "Alice" borrows the copy with barcode "EJ-001"
    Then a loan is created for member "m-001" and copy "EJ-001"

  # ── Edge Cases & Failures ──

  Scenario: Reject loan when borrowing limit reached
    Given "Alice" has 3 active loans
    When "Alice" borrows the copy with barcode "EJ-001"
    Then the loan is rejected with reason "Borrowing limit reached"
    And no LoanCreated event is published
    And the copy "EJ-001" remains Available

  Scenario: Reject loan for unavailable copy
    Given the copy with barcode "EJ-001" is already Borrowed
    When "Alice" borrows the copy with barcode "EJ-001"
    Then the loan is rejected with reason "Copy is not available"

  Scenario: Reject loan for unknown member
    When a member with ID "m-999" borrows the copy with barcode "EJ-001"
    Then the loan is rejected with reason "Member not found"
