Feature: Company Invites - GET, POST /companies/{id}/invites/ and POST /invites/{token}/accept/

  Background:
    Given the base URL is configured
    And a registered user exists with email "inviteadmin@example.com" and password "SecurePass123!"
    And I log in with email "inviteadmin@example.com" and password "SecurePass123!"
    And I have a company created with name "Invite Test Corp"

  # ─────────────────────────────────────────────
  # GET /companies/{id}/invites/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can list company invites
    Given an invite exists for email "listed@example.com" with role "student"
    When the admin requests the list of company invites
    Then the response status should be 200
    And the response should be a list
    And the invite list should contain required fields

  @regression
  Scenario: Invite list is empty when no invites exist
    When the admin requests the list of company invites
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: Non-admin member cannot list invites
    Given a second user is a member of the company with email "memberlist@example.com" and password "SecurePass123!"
    When a non-admin requests the list of company invites
    Then the response status should be 403

  @regression
  Scenario: Unauthenticated user cannot list invites
    When an unauthenticated user requests the list of company invites
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /companies/{id}/invites/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can create an invite for a new user
    When the admin sends an invite to "newinvite@example.com" with role "student"
    Then the response status should be 201
    And the invite response should contain required fields
    And the response body field "email" should equal "newinvite@example.com"
    And the response body field "role" should equal "student"

  @regression
  Scenario: Admin can create an invite with instructor role
    When the admin sends an invite to "newinstructor@example.com" with role "instructor"
    Then the response status should be 201
    And the response body field "role" should equal "instructor"

  @regression
  Scenario: Admin can create an invite with admin role
    When the admin sends an invite to "newadmin2@example.com" with role "admin"
    Then the response status should be 201
    And the response body field "role" should equal "admin"

  @regression
  Scenario: Invite fails when email is already a member
    Given a second user is a member of the company with email "alreadymember@example.com" and password "SecurePass123!"
    When the admin sends an invite to "alreadymember@example.com" with role "student"
    Then the response status should be 400
    And the response body field "email" should contain error "already a member"

  @regression
  Scenario: Invite fails when email already has a pending invite
    Given an invite exists for email "pending@example.com" with role "student"
    When the admin sends an invite to "pending@example.com" with role "instructor"
    Then the response status should be 400
    And the response body field "email" should contain error "pending invite"

  @regression
  Scenario: Invite duplicate check is case-insensitive
    Given an invite exists for email "casecheck@example.com" with role "student"
    When the admin sends an invite to "CASECHECK@EXAMPLE.COM" with role "student"
    Then the response status should be 400
    And the response body field "email" should contain error "pending invite"

  @regression
  Scenario: Invite fails when email is missing
    When I send an authenticated POST request to "/companies/{company_id}/invites/" with body:
      """
      {
        "role": "student"
      }
      """
    Then the response status should be 400
    And the response body field "email" should contain error "required"

  @regression
  Scenario: Invite fails with invalid role
    When the admin sends an invite to "invalidrole2@example.com" with role "superuser"
    Then the response status should be 400
    And the response body should contain a field error for "role"

  @regression
  Scenario: Non-admin cannot create an invite
    Given a second user is a member of the company with email "nonadmininvite@example.com" and password "SecurePass123!"
    When a non-admin tries to send an invite to "someone@example.com"
    Then the response status should be 403

  @regression
  Scenario: Unauthenticated user cannot create an invite
    When an unauthenticated user tries to send an invite
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /invites/{token}/accept/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Invited user can accept the invite
    Given an invite exists for email "acceptme@example.com" with role "student"
    And a user is registered with email "acceptme@example.com" and password "SecurePass123!" for invite acceptance
    When the invited user accepts the invite
    Then the response status should be 200
    And the accept response should confirm the user joined

  @regression
  Scenario: Accepting invite twice returns conflict
    Given an invite exists for email "accepttwice@example.com" with role "student"
    And a user is registered with email "accepttwice@example.com" and password "SecurePass123!" for invite acceptance
    When the invited user accepts the invite
    And the invited user tries to accept the invite again
    Then the response status should be 409
    And the response should indicate invite already accepted

  @regression
  Scenario: User with wrong email cannot accept the invite
    Given an invite exists for email "wrongemail@example.com" with role "student"
    When a different user tries to accept the invite
    Then the response status should be 400
    And the response should indicate email mismatch

  @regression
  Scenario: Unauthenticated user cannot accept an invite
    Given an invite exists for email "unauthaccept@example.com" with role "student"
    When an unauthenticated user tries to accept the invite
    Then the response status should be 401
    And the response body should contain an authentication error

  @regression
  Scenario: Accepting with an invalid token returns 404
    When a user tries to accept an invite with an invalid token
    Then the response status should be 404