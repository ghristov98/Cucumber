Feature: Attempts - POST /tests/{slug}/attempts/ and PATCH /tests/{slug}/attempts/{id}/

  Background:
    Given the base URL is configured
    And a registered user exists with email "attemptauthor@example.com" and password "SecurePass123!"
    And I log in with email "attemptauthor@example.com" and password "SecurePass123!"
    And I have a test created with title "Attempt Test"
    And the test has a multiple choice question with a known answer

  # ─────────────────────────────────────────────
  # POST /tests/{slug}/attempts/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Anonymous user can start an attempt with a name
    When an anonymous user starts an attempt with name "John Doe"
    Then the response status should be 201
    And the attempt response should contain required fields
    And the response body field "anonymous_name" should equal "John Doe"
    And the response body field "attempt_number" should equal "1"

  @smoke @regression
  Scenario: Authenticated user can start an attempt
    When an authenticated user starts an attempt
    Then the response status should be 201
    And the attempt response should contain required fields


  @regression
  Scenario: Anonymous user can start attempt without providing a name
    When an anonymous user starts an attempt with name ""
    Then the response status should be 201
    And the attempt response should contain required fields

  @regression
  Scenario: Attempt is rejected when max attempts limit is reached
    Given I have started an anonymous attempt
    And I have started an anonymous attempt
    And I have started an anonymous attempt
    When the user starts 1 more attempts to exceed the limit
    Then the attempt should be rejected with max attempts error

  @regression
  Scenario: Attempt is rejected when max attempts limit is reached
    Given I have a test created with max attempts of 1
    And I have started an anonymous attempt
    When the user starts 1 more attempts to exceed the limit
    Then the attempt should be rejected with max attempts error

  # ─────────────────────────────────────────────
  # PATCH /tests/{slug}/attempts/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Owner can save draft answers
    Given I have started an anonymous attempt
    When I save draft answers for the attempt
    Then the response status should be 200
    And the draft answers should be saved

  @regression
  Scenario: Non-owner cannot patch the attempt
    Given I have started an anonymous attempt
    When a different user tries to patch the attempt
    Then the permission error should be returned