@epic:lending @context:lending
Feature: Calculate Overdue Fees
  As a librarian
  I want overdue fees to be calculated automatically when a book is returned late
  So that the library can recover costs and encourage timely returns

  Background:
    Given a member "Alice" with Member ID "m-001"
    And the catalog contains a book with ISBN "9780134685991" titled "Effective Java"
    And the book has a copy with barcode "EJ-001"
    And the daily rate for overdue fees is €0.50

  # ── Happy Path ──

  Scenario: Fee is calculated for a book returned 3 days overdue
    Given "Alice" has an overdue loan for copy "EJ-001" that is 3 days overdue
    When "Alice" returns copy "EJ-001"
    Then a Fee of €1.50 is charged to "Alice"
    And a FeeCharged event is published with the loan ID, Member ID "m-001", amount €1.50, and 3 days overdue

  Scenario: Fee is calculated for a book returned 10 days overdue
    Given "Alice" has an overdue loan for copy "EJ-001" that is 10 days overdue
    When "Alice" returns copy "EJ-001"
    Then a Fee of €5.00 is charged to "Alice"
    And a FeeCharged event is published with the loan ID, Member ID "m-001", amount €5.00, and 10 days overdue

  # ── Variations ──

  Scenario: Minimum fee for a book returned 1 day overdue
    Given "Alice" has an overdue loan for copy "EJ-001" that is 1 day overdue
    When "Alice" returns copy "EJ-001"
    Then a Fee of €0.50 is charged to "Alice"
    And a FeeCharged event is published with the loan ID, Member ID "m-001", amount €0.50, and 1 day overdue

  Scenario: No fee when a book is returned on time
    Given "Alice" has an active loan for copy "EJ-001" created 7 days ago
    When "Alice" returns copy "EJ-001"
    Then no Fee is charged
    And no FeeCharged event is published

  Scenario: No fee when a book is returned exactly on the due date
    Given "Alice" has an active loan for copy "EJ-001" created 14 days ago
    When "Alice" returns copy "EJ-001"
    Then no Fee is charged
    And no FeeCharged event is published

  # ── Edge Cases & Failures ──

  Scenario: Fee accumulates over a long overdue period
    Given "Alice" has an overdue loan for copy "EJ-001" that is 30 days overdue
    When "Alice" returns copy "EJ-001"
    Then a Fee of €15.00 is charged to "Alice"
    And a FeeCharged event is published with the loan ID, Member ID "m-001", amount €15.00, and 30 days overdue

  Scenario: No fee is charged when returning a loan that was already returned
    Given "Alice" had a loan for copy "EJ-001" that was already returned
    When "Alice" returns copy "EJ-001"
    Then the return is rejected with reason "This loan has already been returned"
    And no FeeCharged event is published
