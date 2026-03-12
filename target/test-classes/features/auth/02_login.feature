Feature: User Login - POST /auth/login/

  Background:
    Given the base URL is configured
    And a registered user exists with email "testuser@example.com" and password "SecurePass123!"

  # ───────────────────────────────────────────
  # HAPPY PATH
  # ───────────────────────────────────────────

  @smoke @regression
  Scenario: Successful login with valid credentials
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "testuser@example.com",
        "password": "SecurePass123!"
      }
      """

    Then the response status should be 200
    And the response body should have field "access"
    And the response body should have field "refresh"
    And the "access" token should be a valid JWT format
    And the "refresh" token should be a valid JWT format
    And the response body should NOT contain "password"

  # ───────────────────────────────────────────
  # EMAIL VALIDATION
  # ───────────────────────────────────────────

  @regression
  Scenario: Login fails when email is missing
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "password": "SecurePass123!"
      }
      """
    Then the response status should be 400
    And the response body field "email" should contain error "required"

  @regression
  Scenario: Login fails with invalid email format
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "not-an-email",
        "password": "SecurePass123!"
      }
      """
    Then the response status should be 401

  @regression
  Scenario: Login fails with non-existent email
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "ghost@example.com",
        "password": "SecurePass123!"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ───────────────────────────────────────────
  # PASSWORD VALIDATION
  # ───────────────────────────────────────────

  @regression
  Scenario: Login fails when password is missing
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "john.doe+{time}@example.com"
      }
      """
    Then the response status should be 400
    And the response body field "password" should contain error "required"

  @regression
  Scenario: Login fails with wrong password
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "testuser@example.com",
        "password": "WrongPassword99!"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ───────────────────────────────────────────
  # EDGE CASES
  # ───────────────────────────────────────────

  @regression
  Scenario: Login fails with empty request body
    When I send a POST request to "/auth/login/" with body:
      """
      {}
      """
    Then the response status should be 400

  @regression
  Scenario Outline: Login fails with wrong credentials combinations
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "<email>",
        "password": "<password>"
      }
      """
    Then the response status should be <status>

    Examples:
      | email                    | password        | status |
      | testuser@example.com     | WrongPass123!   | 401    |
      | wrong@example.com        | SecurePass123!  | 401    |
      | wrong@example.com        | WrongPass123!   | 401    |