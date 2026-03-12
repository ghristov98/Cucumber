Feature: User Profile - GET, PATCH, DELETE /auth/me/

  Background:
    Given the base URL is configured
    And a registered user exists with email "profileuser@example.com" and password "SecurePass123!"
    And I log in with email "profileuser@example.com" and password "SecurePass123!"

  # ─────────────────────────────────────────────
  # GET /auth/me/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Get profile of authenticated user
    When I send an authenticated GET request to "/auth/me/"
    Then the response status should be 200
    And the response body should contain the user profile
    And the response body field "email" should equal "profileuser@example.com"
    And the response body should NOT contain sensitive fields

  @regression
  Scenario: Get profile fails without authentication
    When I send an unauthenticated GET request to "/auth/me/"
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Get profile fails with invalid token
    When I send a GET request with invalid token to "/auth/me/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # PATCH /auth/me/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Successfully update first and last name
    When I send an authenticated PATCH request to "/auth/me/" with body:
      """
      {
        "first_name": "Jane",
        "last_name": "Smith"
      }
      """
    Then the response status should be 200
    And the profile field "first_name" should equal "Jane"
    And the profile field "last_name" should equal "Smith"
    And the profile field "email" should equal "profileuser@example.com"
    And the response body should NOT contain sensitive fields

  @regression
  Scenario: Successfully update first name only
    When I send an authenticated PATCH request to "/auth/me/" with body:
      """
      {
        "first_name": "UpdatedName"
      }
      """
    Then the response status should be 200
    And the profile field "first_name" should equal "UpdatedName"

  @regression
  Scenario: Update fails with invalid first_name characters
    When I send an authenticated PATCH request to "/auth/me/" with body:
      """
      {
        "first_name": "John123"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "first_name"

  @regression
  Scenario: Update fails with invalid last_name characters
    When I send an authenticated PATCH request to "/auth/me/" with body:
      """
      {
        "last_name": "Smith@#"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "last_name"

  @regression
  Scenario: Update fails without authentication
    When I send an unauthenticated PATCH request to "/auth/me/" with body:
      """
      {
        "first_name": "Jane"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # DELETE /auth/me/
  # ─────────────────────────────────────────────

  @regression
  Scenario: Delete fails without authentication
    When I send an unauthenticated DELETE request to "/auth/me/"
    Then the response status should be 401
    And the response body should contain an authentication error

  @smoke @regression
  Scenario: Successfully delete authenticated user account
    When I send an authenticated DELETE request to "/auth/me/"
    Then the response status should be 204
    And the response body should be empty

  @regression
  Scenario: Deleted user cannot log in again
    Given I send an authenticated DELETE request to "/auth/me/"
    And the response status should be 204
    When I send a POST request to "/auth/login/" with body:
      """
      {
        "email": "profileuser@example.com",
        "password": "SecurePass123!"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Deleted user token is no longer valid
    Given I send an authenticated DELETE request to "/auth/me/"
    And the response status should be 204
    When I send an authenticated GET request to "/auth/me/"
    Then the response status should be 401