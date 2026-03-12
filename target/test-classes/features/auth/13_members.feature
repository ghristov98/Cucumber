Feature: Company Members - GET, PUT, DELETE /companies/{id}/members/

  Background:
    Given the base URL is configured
    And a registered user exists with email "memberadmin@example.com" and password "SecurePass123!"
    And I log in with email "memberadmin@example.com" and password "SecurePass123!"
    And I have a company created with name "Member Test Corp"

  # ─────────────────────────────────────────────
  # GET /companies/{id}/members/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can list company members
    When I request the list of company members
    Then the response status should be 200
    And the response should be a list
    And the member list should contain required fields

  @regression
  Scenario: Non-member cannot list company members
    Given a second user is registered with email "nonmember2@example.com" and password "SecurePass123!"
    When a non-member requests the list of company members
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot list company members
    When an unauthenticated user requests the list of company members
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # PUT /companies/{id}/members/{user_id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can update a member role to instructor
    Given a second user is a member of the company with email "roleupdate@example.com" and password "SecurePass123!"
    When the admin updates the second member role to "instructor"
    Then the response status should be 200
    And the member response should contain required fields
    And the member role should be "instructor"

  @regression
  Scenario: Admin can update a member role to admin
    Given a second user is a member of the company with email "toadmin@example.com" and password "SecurePass123!"
    When the admin updates the second member role to "admin"
    Then the response status should be 200
    And the member role should be "admin"

  @regression
  Scenario: Admin can update a member role to student
    Given a second user is a member of the company with email "tostudent@example.com" and password "SecurePass123!"
    When the admin updates the second member role to "student"
    Then the response status should be 200
    And the member role should be "student"

  @regression
  Scenario: Non-admin cannot update a member role
    Given a second user is a member of the company with email "cantupdate@example.com" and password "SecurePass123!"
    When a non-admin tries to update the second member role to "instructor"
    Then the response status should be 403

  @regression
  Scenario: Unauthenticated user cannot update a member role
    Given a second user is a member of the company with email "unauthupdate@example.com" and password "SecurePass123!"
    When an unauthenticated user tries to update a member role
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Update fails with invalid role value
    Given a second user is a member of the company with email "invalidrole@example.com" and password "SecurePass123!"
    When the admin updates the second member role to "superuser"
    Then the response status should be 400
    And the response body should contain a field error for "role"

  # ─────────────────────────────────────────────
  # DELETE /companies/{id}/members/{user_id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can remove a member from the company
    Given a second user is a member of the company with email "removeme@example.com" and password "SecurePass123!"
    When the admin removes the second member from the company
    Then the response status should be 204
    And the response body should be empty

  @regression
  Scenario: Non-admin cannot remove a member
    Given a second user is a member of the company with email "cantremove@example.com" and password "SecurePass123!"
    When a non-admin tries to remove the second member
    Then the response status should be 403

  @regression
  Scenario: Unauthenticated user cannot remove a member
    Given a second user is a member of the company with email "unauthremove@example.com" and password "SecurePass123!"
    When an unauthenticated user tries to remove a member
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Cannot remove the last admin from the company
    When the admin tries to remove themselves as the last admin
    Then the response status should be 400