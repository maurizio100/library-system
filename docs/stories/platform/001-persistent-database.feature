@epic:platform @context:platform
Feature: Persistent database storage
  As a librarian
  I want the catalog and lending data to survive application restarts
  So that I do not lose records every time the system is restarted

  # ── Happy Path ──

  Scenario: Catalog data persists after application restart
    Given a book "Clean Code" has been added to the catalog
    When the application is restarted
    Then the book "Clean Code" is still present in the catalog

  Scenario: Lending data persists after application restart
    Given a member "Alice" has borrowed a copy of "Clean Code"
    When the application is restarted
    Then the loan for member "Alice" is still recorded

  # ── Edge Cases ──

  Scenario: No data loss when the application is stopped and started multiple times
    Given a book "The Pragmatic Programmer" has been added to the catalog
    When the application is restarted 3 times
    Then the book "The Pragmatic Programmer" is still present in the catalog
