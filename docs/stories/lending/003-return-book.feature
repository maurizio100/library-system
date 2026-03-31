@epic:lending @context:lending
Feature: Return a Book
  As a member
  I want to return a borrowed book to the library
  So that the copy becomes available for other members again

  Background:
    Given a member "Alice" with Member ID "m-001"
    And the catalog contains a book with ISBN "9780134685991" titled "Effective Java"
    And the book has a copy with barcode "EJ-001"

  # ── Happy Path ──

  Scenario: Successfully return a book on time
    Given "Alice" has an active loan for copy "EJ-001" created 7 days ago
    When "Alice" returns copy "EJ-001"
    Then the loan is marked as Returned
    And the return date is today
    And no Fee is charged
    And a BookReturned event is published with the loan ID, Member ID "m-001", copy barcode "EJ-001", and today's date
    And copy "EJ-001" is marked as Available

  Scenario: Successfully return a book on the due date
    Given "Alice" has an active loan for copy "EJ-001" created 14 days ago
    When "Alice" returns copy "EJ-001"
    Then the loan is marked as Returned
    And no Fee is charged
    And copy "EJ-001" is marked as Available

  # ── Variations ──

  Scenario: Return an overdue book incurs a fee
    Given "Alice" has an overdue loan for copy "EJ-001" that is 5 days overdue
    When "Alice" returns copy "EJ-001"
    Then the loan is marked as Returned
    And a Fee of €2.50 is charged to "Alice"
    And a FeeCharged event is published with the loan ID, Member ID "m-001", amount €2.50, and 5 days overdue
    And a BookReturned event is published with the loan ID, Member ID "m-001", copy barcode "EJ-001", and today's date
    And copy "EJ-001" is marked as Available

  Scenario: Return an overdue book with 1 day overdue
    Given "Alice" has an overdue loan for copy "EJ-001" that is 1 day overdue
    When "Alice" returns copy "EJ-001"
    Then the loan is marked as Returned
    And a Fee of €0.50 is charged to "Alice"
    And a FeeCharged event is published with the loan ID, Member ID "m-001", amount €0.50, and 1 day overdue

  # ── Edge Cases & Failures ──

  Scenario: Reject return for a loan that has already been returned
    Given "Alice" had a loan for copy "EJ-001" that was already returned
    When "Alice" returns copy "EJ-001"
    Then the return is rejected with reason "This loan has already been returned"
    And copy "EJ-001" remains Available

  Scenario: Reject return for an unknown loan
    When member "m-001" returns copy "UNKNOWN-001"
    Then the return is rejected with reason "No active loan found for this copy"
