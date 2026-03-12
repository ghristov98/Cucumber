Feature: Company Tests - GET, POST, PATCH, DELETE /tests/company/{id}/

  Background:
    Given the base URL is configured
    And a registered user exists with email "companytestadmin@example.com" and password "SecurePass123!"
    And I log in with email "companytestadmin@example.com" and password "SecurePass123!"
    And I have a company created with name "Company Test Corp"

  # ─────────────────────────────────────────────
  # GET /tests/company/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can list company tests
    Given I have a company test created with title "Listed Company Test"
    When I request the list of company tests
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: Student member can list company tests
    Given a student is a member of the company with email "studentlisttest@example.com" and password "SecurePass123!"
    When a student member requests the list of company tests
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: Non-member cannot list company tests
    Given a second user is registered with email "nonmembertest@example.com" and password "SecurePass123!"
    When a non-member requests the list of company tests
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot list company tests
    When I send an unauthenticated GET request to "/tests/company/{id}/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /tests/company/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can create a company test
    When I create a company test with title "Admin Company Test"
    Then the response status should be 201
    And the company test response should contain required fields

  @smoke @regression
  Scenario: Instructor can create a company test
    Given an instructor is a member of the company with email "testinstructor@example.com" and password "SecurePass123!"
    When an instructor creates a company test with title "Instructor Company Test"
    Then the response status should be 201
    And the company test response should contain required fields

  @regression
  Scenario: Student cannot create a company test
    Given a student is a member of the company with email "teststudent@example.com" and password "SecurePass123!"
    When a student tries to create a company test with title "Student Test Attempt"
    Then the response should indicate students cannot create tests

  @regression
  Scenario: Company test visibility is automatically set to link_only
    When I create a company test with title "Visibility Test"
    Then the response status should be 201
    And the response body field "visibility" should equal "link_only"

  @regression
  Scenario: Create company test fails when title is missing
    When I send an authenticated POST request to "/tests/company/{company_id}/" with body:
      """
      {
        "description": "No title",
        "folder": null
      }
      """
    Then the response status should be 400
    And the response body field "title" should contain error "required"

  # ─────────────────────────────────────────────
  # GET /tests/company/{id}/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can get company test details
    Given I have a company test created with title "Detail Test"
    When I request the company test details
    Then the response status should be 200
    And the response body should have field "questions"

  @regression
  Scenario: Student can get company test details
    Given I have a company test created with title "Student Viewable Test"
    And a student is a member of the company with email "detailstudent@example.com" and password "SecurePass123!"
    When a student member requests the company test details
    Then the response status should be 200

  @regression
  Scenario: Non-member cannot get company test details
    Given I have a company test created with title "Private Company Test"
    And a second user is registered with email "nonmemberdetail@example.com" and password "SecurePass123!"
    When a non-member requests the company test details
    Then the response status should be 404

  # ─────────────────────────────────────────────
  # PATCH /tests/company/{id}/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can update company test
    Given I have a company test created with title "Old Company Title"
    When I update the company test title to "New Company Title"
    Then the response status should be 200
    And the profile field "title" should equal "New Company Title"

  @smoke @regression
  Scenario: Instructor can update company test
    Given I have a company test created with title "Instructor Update Test"
    And an instructor is a member of the company with email "updateinstructor@example.com" and password "SecurePass123!"
    When an instructor updates the company test title to "Instructor Updated Title"
    Then the response status should be 200

  @regression
  Scenario: Student cannot update company test created by admin
    Given I have a company test created with title "Admin Protected Test"
    And a student is a member of the company with email "updatestudent@example.com" and password "SecurePass123!"
    When a student tries to update the company test title to "Hacked Title"
    Then the response status should be 403

  # ─────────────────────────────────────────────
  # DELETE /tests/company/{id}/{slug}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can delete a company test
    Given I have a company test created with title "Deletable Company Test"
    When I delete the company test
    Then the response status should be 204
    And the response body should be empty
    And the deleted company test should no longer be accessible

  @regression
  Scenario: Student cannot delete company test
    Given I have a company test created with title "No Delete Student Test"
    And a student is a member of the company with email "deletestudent@example.com" and password "SecurePass123!"
    When a student tries to delete the company test
    Then the response status should be 403