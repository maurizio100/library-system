@epic:lending @context:lending @layer:frontend
Feature: Sidebar Navigation for Lending Pages
  As a librarian
  I want lending actions organized as dedicated pages in the sidebar
  So that I can access borrowing, returning, and member management without navigating through a single page

  Background:
    Given the library application is open in the browser
    And the sidebar is visible with a "Catalog" section already in place

  # ── Happy Path ──

  Scenario: Sidebar displays lending navigation links
    When the librarian views any page
    Then the sidebar contains a "Lending" section with links "Borrow a Book" and "Return a Book"
    And the sidebar contains a "Members" section with links "All Members" and "Register Member"

  Scenario: Navigate to Borrow a Book page via sidebar
    Given the librarian is on the "Browse Books" page
    When the librarian clicks "Borrow a Book" in the sidebar
    Then the browser URL changes to "/lending/borrow"
    And the Borrow a Book form is displayed

  Scenario: Navigate to Register Member page via sidebar
    Given the librarian is on the "Browse Books" page
    When the librarian clicks "Register Member" in the sidebar
    Then the browser URL changes to "/members/register"
    And the Register Member form is displayed

  Scenario: Active lending page is highlighted in the sidebar
    Given the librarian navigates to the "Borrow a Book" page
    Then the "Borrow a Book" link in the sidebar is visually highlighted as the active page
    And the "Return a Book" link is not highlighted

  # ── Variations ──

  Scenario: Sidebar sections are visually grouped
    When the librarian views the sidebar
    Then the "Catalog" section appears first
    And the "Lending" section appears second
    And the "Members" section appears third
    And each section has a visible heading separating it from the others

  Scenario: Direct URL access loads the correct lending page
    When the librarian opens the URL "/lending/borrow" directly in the browser
    Then the Borrow a Book page is displayed
    And the sidebar shows "Borrow a Book" as the active link

  Scenario: Navigate between lending and catalog sections
    Given the librarian is on the "Borrow a Book" page
    When the librarian clicks "Browse Books" in the sidebar
    Then the Browse Books page is displayed
    And the "Browse Books" link is highlighted instead of "Borrow a Book"

  # ── Edge Cases & Failures ──

  Scenario: Return a Book page shows placeholder when not yet implemented
    When the librarian clicks "Return a Book" in the sidebar
    Then the browser URL changes to "/lending/return"
    And a message "Coming soon" is displayed in the main content area

  Scenario: Sidebar collapses gracefully on narrow screens
    Given the browser window is narrower than 768 pixels
    When the librarian views any page
    Then the sidebar collapses into a hamburger menu icon
    And tapping the icon reveals the full sidebar as an overlay
