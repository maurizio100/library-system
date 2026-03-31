@epic:lending @context:lending
Feature: List All Members
  As a librarian
  I want to retrieve all registered Members
  So that the borrowing interface can offer a searchable member selection

  Background:
    Given a registered member "Alice" with Member ID "m-001"
    And a registered member "Bob" with Member ID "m-002"

  # ── Happy Path ──

  Scenario: Retrieve all registered members
    When the librarian requests the list of all Members
    Then the response contains 2 Members
    And the response includes member "Alice" with Member ID "m-001"
    And the response includes member "Bob" with Member ID "m-002"

  # ── Variations ──

  Scenario: Member list includes name and Member ID
    When the librarian requests the list of all Members
    Then each Member entry contains the name and Member ID

  # ── Edge Cases & Failures ──

  Scenario: Empty member list when no members are registered
    Given no Members are registered
    When the librarian requests the list of all Members
    Then the response contains 0 Members
