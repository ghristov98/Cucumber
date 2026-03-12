Feature: Results - GET /tests/{slug}/results/

  Background:
    Given the base URL is configured
    And a registered user exists with email "resultsauthor@example.com" and password "SecurePass123!"
    And I log in with email "resultsauthor@example.com" and password "SecurePass123!"
    And I have a test created with title "Results Test"
    And the test has a multiple choice question with a known answer
    And I have started an anonymous attempt
    And I submit the attempt with correct answers

  # ─────────────────────────────────────────────
  # GET /tests/{slug}/results/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can get all results for their test
    When the author requests all results for the test
    Then the response status should be 200
    And the response should be a list
    And the results list should contain required fields

  @regression
  Scenario: Non-author cannot access test results
    When an unauthenticated user requests all results for the test
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # GET /tests/{slug}/results/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can get detailed result for a specific attempt
    When the author requests result details for the attempt
    Then the response status should be 200
    And the result detail should contain answers breakdown

  @regression
  Scenario: Result detail includes correct and submitted answer IDs
    When the author requests result details for the attempt
    Then the response status should be 200
    And the response body should have field "answers"