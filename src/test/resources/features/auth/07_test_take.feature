Feature: Take Test - GET /api/tests/{slug}/take/ and POST /api/tests/{slug}/verify-password/

  Background:
    Given the base URL is configured
    And a registered user exists with email "testauthor@example.com" and password "SecurePass123!"
    And I log in with email "testauthor@example.com" and password "SecurePass123!"

  # ─────────────────────────────────────────────
  # GET /tests/{slug}/take/ — public test
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Anyone can take a link_only test without authentication
    Given I have a test created with title "Take Me Test"
    When I send an unauthenticated GET request to take the created test
    Then the response status should be 200
    And the response body should have field "questions"
    And the take test response should not contain is_correct field

  # ─────────────────────────────────────────────
  # GET /tests/{slug}/take/ — password protected
  # ─────────────────────────────────────────────

  @regression
  Scenario: Password protected test returns 403 without verification
    Given I have a password protected test with title "Secret Test" and password "Secret123!"
    When I send an unauthenticated GET request to take the created test
    Then the response status should be 403
    And the response should require a password

  @smoke @regression
  Scenario: Password protected test is accessible after password verification
    Given I have a password protected test with title "Secret Test 2" and password "Secret123!"
    When I verify the password "Secret123!" for the created test
    And I send a GET request to take the created test with verified password header
    Then the response status should be 200
    And the take test response should not contain is_correct field

  # ─────────────────────────────────────────────
  # POST /tests/{slug}/verify-password/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Correct password verification returns verified token
    Given I have a password protected test with title "Verify Me" and password "Correct123!"
    When I verify the password "Correct123!" for the created test
    Then the response status should be 200
    And the password verification should succeed

  @regression
  Scenario: Wrong password returns 400 with error
    Given I have a password protected test with title "Wrong Pass Test" and password "Correct123!"
    When I verify the password "WrongPassword!" for the created test
    Then the response status should be 400
    And the response body field "password" should contain error "Invalid password"