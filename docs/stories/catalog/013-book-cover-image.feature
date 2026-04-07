@epic:catalog @context:catalog
Feature: Book cover image
  As a librarian
  I want to see a cover image on each book's details page
  So that I can identify books visually at a glance

  # ── Happy Path ──

  Scenario: Cover image is shown when the book was added via ISBN lookup
    Given the book "978-0-13-468599-1" was added to the catalog via ISBN lookup
    And the ISBN lookup returned a cover image for "978-0-13-468599-1"
    When the librarian opens the details page for "978-0-13-468599-1"
    Then the cover image is displayed on the page

  # ── Edge Cases ──

  Scenario: No cover image is shown when the book was added manually
    Given the book "978-0-13-468599-1" was added to the catalog manually without a cover image
    When the librarian opens the details page for "978-0-13-468599-1"
    Then no cover image is displayed
    And a placeholder is shown instead

  Scenario: No cover image is shown when the ISBN lookup returned no cover
    Given the book "978-0-201-63361-0" was added to the catalog via ISBN lookup
    And the ISBN lookup returned no cover image for "978-0-201-63361-0"
    When the librarian opens the details page for "978-0-201-63361-0"
    Then no cover image is displayed
    And a placeholder is shown instead
