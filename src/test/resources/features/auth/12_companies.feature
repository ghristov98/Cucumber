Feature: Companies - GET, POST, PATCH, DELETE /companies/

  Background:
    Given the base URL is configured
    And a registered user exists with email "companyadmin@example.com" and password "SecurePass123!"
    And I log in with email "companyadmin@example.com" and password "SecurePass123!"

  # ─────────────────────────────────────────────
  # GET /companies/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Authenticated user can list their companies
    Given I have a company created with name "Listed Corp"
    When I request the list of my companies
    Then the response status should be 200
    And the response should be a list
    And the company list should contain required fields

  @regression
  Scenario: List companies returns empty list when user has no companies
    When I request the list of my companies
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: List companies fails without authentication
    When I send an unauthenticated GET request to "/companies/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /companies/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Successfully create a company
    When I send a create company request with name "ACME Corp"
    Then the response status should be 201
    And the company response should contain required fields
    And the company creator should have admin role
    And the response body field "name" should equal "ACME Corp"
    And the response body field "member_count" should equal "1"

  @regression
  Scenario: Create company fails when name is missing
    When I send an authenticated POST request to "/companies/" with body:
      """
      {}
      """
    Then the response status should be 400
    And the response body field "name" should contain error "required"

  @regression
  Scenario: Create company fails with empty name
    When I send an authenticated POST request to "/companies/" with body:
      """
      {
        "name": ""
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "name"

  @regression
  Scenario: Create company fails without authentication
    When an unauthenticated user sends a create company request
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # GET /companies/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can get company details
    Given I have a company created with name "Detail Corp"
    When I request the company details
    Then the response status should be 200
    And the company response should contain required fields
    And the response body field "name" should equal "Detail Corp"

  @regression
  Scenario: Non-member cannot access company details
    Given I have a company created with name "Private Corp"
    And a second user is registered with email "nonmember@example.com" and password "SecurePass123!"
    When a non-member requests the company details
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot access company details
    Given I have a company created with name "Auth Corp"
    When an unauthenticated user requests the company details
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # PATCH /companies/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can update company name
    Given I have a company created with name "Old Name Corp"
    When I update the company name to "New Name Corp"
    Then the response status should be 200
    And the response body field "name" should equal "New Name Corp"

  @regression
  Scenario: Update fails with empty name
    Given I have a company created with name "Valid Corp"
    When I send an authenticated PATCH request to "/companies/{id}/" with body:
      """
      {
        "name": ""
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "name"

  @regression
  Scenario: Non-admin cannot update company
    Given I have a company created with name "Protected Corp"
    And a second user is registered with email "nonadmin@example.com" and password "SecurePass123!"
    When a non-admin tries to update the company name to "Hacked Corp"
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot update company
    Given I have a company created with name "Secure Corp"
    When an unauthenticated user tries to update the company
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # DELETE /companies/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can delete company
    Given I have a company created with name "Deletable Corp"
    When I delete the company
    Then the response status should be 204
    And the response body should be empty
    And the deleted company should no longer be accessible

  @regression
  Scenario: Non-admin cannot delete company
    Given I have a company created with name "Safe Corp"
    And a second user is registered with email "nodelete@example.com" and password "SecurePass123!"
    When a non-admin tries to delete the company
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot delete company
    Given I have a company created with name "Protected Delete Corp"
    When an unauthenticated user tries to delete the company
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Deleted company is no longer listed
    Given I have a company created with name "Gone Corp"
    When I delete the company
    And I request the list of my companies
    Then the response status should be 200
    And the response should be a list