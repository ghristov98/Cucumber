Feature: Refresh Access Token - POST /auth/refresh/

  Background:
    Given the base URL is configured
    And a registered user exists with email "refreshuser@example.com" and password "SecurePass123!"

  # ───────────────────────────────────────────
  # HAPPY PATH
  # ───────────────────────────────────────────

  @smoke @regression
  Scenario: Successfully refresh an access token
    Given I log in with email "refreshuser@example.com" and password "SecurePass123!"
    When I send the saved refresh token to "/auth/refresh/"
    Then the response status should be 200
    And the response body should have field "access"
    And the "access" token should be a valid JWT format
    And the new access token should be different from the original
    And the response body should NOT contain "refresh"

  # ───────────────────────────────────────────
  # TOKEN VALIDATION
  # ───────────────────────────────────────────

  @regression
  Scenario: Refresh fails when token is missing
    When I send a POST request to "/auth/refresh/" with body:
      """
      {}
      """
    Then the response status should be 400
    And the response body field "refresh" should contain error "required"

  @regression
  Scenario: Refresh fails with an invalid token
    When I send a POST request to "/auth/refresh/" with body:
      """
      {
        "refresh": "this.is.not.a.valid.token"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Refresh fails with a malformed token
    When I send a POST request to "/auth/refresh/" with body:
      """
      {
        "refresh": "notavalidjwtatall"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Refresh fails with an empty token value
    When I send a POST request to "/auth/refresh/" with body:
      """
      {
        "refresh": ""
      }
      """
    Then the response status should be 400
    And the response body field "refresh" should contain error "may not be blank"