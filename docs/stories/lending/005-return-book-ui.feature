@epic:lending @context:lending @layer:frontend
Feature: Return a Book via the UI
  As a librarian
  I want to process a Return through the library's web interface
  So that I can handle book returns and see any Fees owed

  Background:
    Given a registered member "Alice" with Member ID "m-001"
    And the catalog contains a book "Effective Java" with ISBN "9780134685991"
    And the book has a copy with barcode "EJ-001"

  # ── Happy Path ──

  Scenario: Successfully return a book on time through the form
    Given "Alice" has an active Loan for copy "EJ-001" created 7 days ago
    And the librarian navigates to the "Return a Book" page
    When the librarian enters copy barcode "EJ-001"
    And the librarian confirms the return
    Then a success message is displayed confirming the Return
    And the return summary shows no Fee was charged
    And the copy "EJ-001" is shown as Available

  # ── Variations ──

  Scenario: Return an overdue book displays the fee
    Given "Alice" has an overdue Loan for copy "EJ-001" that is 5 days overdue
    And the librarian navigates to the "Return a Book" page
    When the librarian enters copy barcode "EJ-001"
    And the librarian confirms the return
    Then a success message is displayed confirming the Return
    And the return summary shows a Fee of €2.50

  Scenario: Return an overdue book with 1 day overdue displays minimal fee
    Given "Alice" has an overdue Loan for copy "EJ-001" that is 1 day overdue
    And the librarian navigates to the "Return a Book" page
    When the librarian enters copy barcode "EJ-001"
    And the librarian confirms the return
    Then the return summary shows a Fee of €0.50

  # ── Edge Cases & Failures ──

  Scenario: Display error when no active loan exists for the copy
    Given the librarian navigates to the "Return a Book" page
    When the librarian enters copy barcode "UNKNOWN-001"
    And the librarian confirms the return
    Then an error message "No active loan found for this copy" is displayed

  Scenario: Display error when loan was already returned
    Given "Alice" had a Loan for copy "EJ-001" that was already returned
    And the librarian navigates to the "Return a Book" page
    When the librarian enters copy barcode "EJ-001"
    And the librarian confirms the return
    Then an error message "This loan has already been returned" is displayed

  Scenario: Prevent submission with empty barcode
    Given the librarian navigates to the "Return a Book" page
    When the librarian leaves the copy barcode field empty
    Then the confirm button is disabled
