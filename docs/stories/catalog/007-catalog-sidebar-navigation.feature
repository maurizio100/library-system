@epic:catalog @context:catalog @layer:frontend
Feature: Sidebar Navigation for Catalog Pages
  As a librarian
  I want a persistent sidebar that organizes catalog actions into dedicated pages
  So that I can navigate directly to the task I need without returning to a single crowded home page

  Background:
    Given the library application is open in the browser

  # ── Happy Path ──

  Scenario: Sidebar displays catalog navigation links
    When the librarian views any page
    Then a sidebar is visible on the left side of the screen
    And the sidebar contains a "Catalog" section with links "Browse Books" and "Add Book"

  Scenario: Navigate to Browse Books page via sidebar
    Given the librarian is on the "Add Book" page
    When the librarian clicks "Browse Books" in the sidebar
    Then the browser URL changes to "/catalog/browse"
    And the Browse Books page is displayed with the book list and search field

  Scenario: Navigate to Add Book page via sidebar
    Given the librarian is on the "Browse Books" page
    When the librarian clicks "Add Book" in the sidebar
    Then the browser URL changes to "/catalog/add"
    And the Add Book form is displayed

  Scenario: Active page is highlighted in the sidebar
    Given the librarian navigates to the "Browse Books" page
    Then the "Browse Books" link in the sidebar is visually highlighted as the active page
    And the "Add Book" link is not highlighted

  # ── Variations ──

  Scenario: Sidebar persists across page transitions
    Given the librarian is on the "Browse Books" page
    When the librarian clicks "Add Book" in the sidebar
    Then the sidebar remains visible and does not reload
    And only the main content area changes to the Add Book form

  Scenario: Direct URL access loads the correct page
    When the librarian opens the URL "/catalog/add" directly in the browser
    Then the Add Book page is displayed
    And the sidebar shows "Add Book" as the active link

  Scenario: Root URL redirects to Browse Books
    When the librarian opens the URL "/"
    Then the browser redirects to "/catalog/browse"
    And the Browse Books page is displayed

  # ── Edge Cases & Failures ──

  Scenario: Unknown URL shows a not-found message
    When the librarian opens the URL "/catalog/unknown"
    Then a "Page not found" message is displayed
    And the sidebar remains visible for navigation

  Scenario: Sidebar is accessible via keyboard
    When the librarian uses the Tab key to navigate the sidebar
    Then each sidebar link receives focus in order
    And pressing Enter on a focused link navigates to that page
