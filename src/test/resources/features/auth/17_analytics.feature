Feature: Analytics - GET /analytics/tests/{slug}/

  Background:
    Given the base URL is configured
    And a registered user exists with email "analyticsauthor@example.com" and password "SecurePass123!"
    And I log in with email "analyticsauthor@example.com" and password "SecurePass123!"
    And I have a test created with title "Analytics Test"
    And the test has at least one submitted attempt

  # ─────────────────────────────────────────────
  # GET /analytics/tests/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can get analytics for their test
    When the author requests analytics for the test
    Then the response status should be 200
    And the analytics response should contain required fields

  @smoke @regression
  Scenario: Analytics question stats contain required fields
    When the author requests analytics for the test
    Then the response status should be 200
    And the analytics question stats should contain required fields

  @regression
  Scenario: Total attempts is at least 1 after a submission
    When the author requests analytics for the test
    Then the response status should be 200
    And the total attempts should be at least 1

  @regression
  Scenario: Completion rate is within valid range
    When the author requests analytics for the test
    Then the response status should be 200
    And the completion rate should be between 0 and 100

  @regression
  Scenario: Pass rate is within valid range
    When the author requests analytics for the test
    Then the response status should be 200
    And the pass rate should be between 0 and 100

  @regression
  Scenario: Non-author cannot access test analytics
    Given a second user is registered with email "nonanalyticsuser@example.com" and password "SecurePass123!"
    When a non-author requests analytics for the test
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot access analytics
    When an unauthenticated user requests analytics for the test
    Then the response status should be 401
    And the response body should contain an authentication error