Feature: Company Folders - GET, POST, PATCH, DELETE /companies/{id}/folders/

  Background:
    Given the base URL is configured
    And a registered user exists with email "folderadmin@example.com" and password "SecurePass123!"
    And I log in with email "folderadmin@example.com" and password "SecurePass123!"
    And I have a company created with name "Folder Test Corp"

  # ─────────────────────────────────────────────
  # GET /companies/{id}/folders/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can list company folders
    Given I have a folder created with name "Listed Folder"
    When I request the list of company folders
    Then the response status should be 200
    And the response should be a list
    And the folder list should contain required fields

  @regression
  Scenario: Student member can list company folders
    Given a student is a member of the company with email "studentlist@example.com" and password "SecurePass123!"
    When a student member requests the list of company folders
    Then the response status should be 200
    And the response should be a list

  @regression
  Scenario: Non-member cannot list company folders
    Given a second user is registered with email "nonmemberfolder@example.com" and password "SecurePass123!"
    When a non-member requests the list of company folders
    Then the response status should be 404

  @regression
  Scenario: Unauthenticated user cannot list company folders
    When I send an unauthenticated GET request to "/companies/{id}/folders/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /companies/{id}/folders/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can create a folder
    When I send a create folder request with name "Admin Folder"
    Then the response status should be 201
    And the folder response should contain required fields
    And the response body field "name" should equal "Admin Folder"

  @smoke @regression
  Scenario: Instructor can create a folder
    Given an instructor is a member of the company with email "foldinstructor@example.com" and password "SecurePass123!"
    When an instructor creates a folder with name "Instructor Folder"
    Then the response status should be 201
    And the folder response should contain required fields

  @regression
  Scenario: Student cannot create a folder
    Given a student is a member of the company with email "foldstudent@example.com" and password "SecurePass123!"
    When a student tries to create a folder with name "Student Folder"
    Then the response status should be 403

  @regression
  Scenario: Create folder fails when name is missing
    When I send an authenticated POST request to "/companies/{company_id}/folders/" with body:
      """
      {
        "parent": null
      }
      """
    Then the response status should be 400
    And the response body field "name" should contain error "required"

  @smoke @regression
  Scenario: Admin can create a child folder under a parent
    Given I have a folder created with name "Parent Folder"
    When I create a child folder under the first folder
    Then the response status should be 201
    And the folder response should contain required fields

  @regression
  Scenario: Unauthenticated user cannot create a folder
    When I send an unauthenticated POST request to "/companies/{id}/folders/" with body:
      """
      {
        "name": "Unauth Folder",
        "parent": null
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # PATCH /companies/{id}/folders/{folder_id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can update a folder name
    Given I have a folder created with name "Old Folder Name"
    When I update the folder name to "New Folder Name"
    Then the response status should be 200
    And the response body field "name" should equal "New Folder Name"

  @regression
  Scenario: Update fails when folder references itself as parent
    Given I have a folder created with name "Self Parent Folder"
    When I try to set the folder's parent to itself
    Then the response status should be 400
    And the response should indicate folder cannot be its own parent

  @regression
  Scenario: Update fails when circular hierarchy is detected
    Given I have a folder created with name "Folder A"
    And I create a child folder under the first folder
    When I try to create a circular folder hierarchy
    Then the response status should be 400
    And the response should indicate circular hierarchy is not allowed

  @regression
  Scenario: Student cannot update a folder
    Given I have a folder created with name "Protected Folder"
    And a student is a member of the company with email "foldstudentpatch@example.com" and password "SecurePass123!"
    When a student tries to update the folder name to "Hacked Folder"
    Then the response status should be 403

  # ─────────────────────────────────────────────
  # DELETE /companies/{id}/folders/{folder_id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Admin can delete a folder
    Given I have a folder created with name "Deletable Folder"
    When I delete the folder
    Then the response status should be 204
    And the response body should be empty
    And the deleted folder should no longer be accessible

  @regression
  Scenario: Student cannot delete a folder
    Given I have a folder created with name "Safe Folder"
    And a student is a member of the company with email "foldstudentdel@example.com" and password "SecurePass123!"
    When a student tries to delete the folder
    Then the response status should be 403

  @regression
  Scenario: Unauthenticated user cannot delete a folder
    Given I have a folder created with name "Auth Folder"
    When an unauthenticated user tries to delete the folder
    Then the response status should be 401
    And the response body should contain an authentication error