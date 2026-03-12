Feature: Test Detail - GET, PATCH, DELETE /api/tests/{slug}/

  Background:
    Given the base URL is configured
    And a registered user exists with email "testauthor@example.com" and password "SecurePass123!"
    And I log in with email "testauthor@example.com" and password "SecurePass123!"
    And I have a test created with title "Detail Test"

  # ─────────────────────────────────────────────
  # GET /tests/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can get test details
    When I send an authenticated GET request to the created test
    Then the response status should be 200
    And the test response should contain required detail fields
    And the response body should have field "questions"

  @regression
  Scenario: Get test details fails without authentication
    When I send an unauthenticated GET request to "/tests/some-fake-slug/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # PATCH /tests/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can update test title
    When I send an authenticated PATCH request to the created test with body:
      """
      {
        "title": "Updated Title"
      }
      """
    Then the response status should be 200
    And the profile field "title" should equal "Updated Title"

  @regression
  Scenario: Author can update visibility to public
    When I send an authenticated PATCH request to the created test with body:
      """
      {
        "visibility": "public"
      }
      """
    Then the response status should be 200
    And the profile field "visibility" should equal "public"

  @regression
  Scenario: Update fails with invalid visibility
    When I send an authenticated PATCH request to the created test with body:
      """
      {
        "visibility": "secret"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "visibility"

  @regression
  Scenario: Update fails without authentication
    When I send an unauthenticated PATCH request to "/tests/some-fake-slug/" with body:
      """
      {
        "title": "Hacked Title"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # DELETE /tests/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can delete their test
    When I send an authenticated DELETE request to the created test
    Then the response status should be 204
    And the response body should be empty
    And the deleted test should no longer be accessible

  @regression
  Scenario: Delete fails without authentication
    When I send an unauthenticated DELETE request to "/tests/some-fake-slug/"
    Then the response status should be 401
    And the response body should contain an authentication error