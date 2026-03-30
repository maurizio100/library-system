@epic:lending @context:lending
Feature: Register a Member
  As a librarian
  I want to register a new library member
  So that they can borrow books from the library

  # ── Happy Path ──

  Scenario: Successfully register a new member
    Given no member is registered with email "alice@example.com"
    When I register a member with name "Alice Thompson" and email "alice@example.com"
    Then a Member is created with name "Alice Thompson" and email "alice@example.com"
    And the Member is assigned a Member ID
    And the Member has a borrowing limit of 3
    And a MemberRegistered event is published with the Member ID, name "Alice Thompson", and email "alice@example.com"

  # ── Variations ──

  Scenario: Register a second member with a different email
    Given a member is already registered with email "alice@example.com"
    When I register a member with name "Bob Martin" and email "bob@example.com"
    Then a Member is created with name "Bob Martin" and email "bob@example.com"
    And the Member is assigned a Member ID

  # ── Edge Cases & Failures ──

  Scenario: Reject registration when email is already taken
    Given a member is already registered with email "alice@example.com"
    When I register a member with name "Alice Duplicate" and email "alice@example.com"
    Then the registration is rejected with reason "A member with this email already exists"
    And no MemberRegistered event is published

  Scenario: Reject registration when email is invalid
    When I register a member with name "Charlie Brown" and email "not-an-email"
    Then the registration is rejected with reason "Invalid email address"
    And no MemberRegistered event is published

  Scenario: Reject registration when name is empty
    When I register a member with name "" and email "empty@example.com"
    Then the registration is rejected with reason "Member name is required"
    And no MemberRegistered event is published
