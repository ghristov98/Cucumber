Feature: Tests - GET, POST /api/tests/

  Background:
    Given the base URL is configured
    And a registered user exists with email "testauthor@example.com" and password "SecurePass123!"
    And I log in with email "testauthor@example.com" and password "SecurePass123!"

  # ─────────────────────────────────────────────
  # GET /tests/ — List tests
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Authenticated user can list their tests
    Given I have a test created with title "My Listed Test"
    When I send an authenticated GET request to "/tests/"
    Then the response status should be 200
    And the response should be a list
    And the test response should contain required list fields

  @regression
  Scenario: List tests fails without authentication
    When I send an unauthenticated GET request to "/tests/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /tests/ — Create test
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Successfully create a test with all fields
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Math Quiz",
        "description": "Basic arithmetic test",
        "visibility": "link_only",
        "time_limit_minutes": 30,
        "max_attempts": 30,
        "show_answers_after": true,
        "folder": null
      }
      """
    Then the response status should be 201
    And the response body should have field "id"
    And the response body should have field "slug"
    And the response body field "title" should equal "Math Quiz"
    And the response body field "visibility" should equal "link_only"

  @regression
  Scenario: Successfully create a public test
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Public Quiz",
        "description": "Everyone can see this",
        "visibility": "public",
        "folder": null
      }
      """
    Then the response status should be 201
    And the response body field "visibility" should equal "public"

  @regression
  Scenario: Successfully create a password protected test
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Secret Quiz",
        "description": "Protected content",
        "visibility": "password_protected",
        "password": "TestPass123!",
        "folder": null
      }
      """
    Then the response status should be 201
    And the response body field "visibility" should equal "password_protected"

  @regression
  Scenario: Create test fails when title is missing
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "description": "No title here",
        "visibility": "link_only"
      }
      """
    Then the response status should be 400
    And the response body field "title" should contain error "required"

  @regression
  Scenario: Create test fails when title exceeds 255 characters
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "visibility": "link_only"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "title"

  @regression
  Scenario: Create test fails with invalid visibility value
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Bad Visibility Test",
        "visibility": "private"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "visibility"

  @regression
  Scenario: Create test fails when password is missing for password_protected visibility
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Protected Without Password",
        "visibility": "password_protected"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "password"

  @regression
  Scenario: Create test fails with time_limit_minutes above maximum
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Too Long Timer",
        "visibility": "link_only",
        "time_limit_minutes": 1441
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "time_limit_minutes"

  @regression
  Scenario: Create test fails with max_attempts above 100
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Too Many Attempts",
        "visibility": "link_only",
        "max_attempts": 101
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "max_attempts"

  @regression
  Scenario: Create test fails without authentication
    When I send an unauthenticated POST request to "/tests/" with body:
      """
      {
        "title": "Unauthorized Test",
        "visibility": "link_only"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # GET /tests/public/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Anyone can list public tests without authentication
    When I send an unauthenticated GET request to "/tests/public/"
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: Public tests list does not expose is_correct
    When I send an unauthenticated GET request to "/tests/public/"
    Then the response status should be 200
    And the take test response should not contain is_correct field

  @regression
  Scenario: Public tests can be filtered by search
    When I send an unauthenticated GET request to "/tests/public/?search=Math"
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: Public tests can be ordered by created_at ascending
    When I send an unauthenticated GET request to "/tests/public/?ordering=created_at"
    Then the response status should be 200

  @regression
  Scenario: Public tests can be ordered by created_at descending
    When I send an unauthenticated GET request to "/tests/public/?ordering=-created_at"
    Then the response status should be 200