Feature: User Registration - POST /auth/register/

  Background:
    Given the base URL is configured

  # Happy path
  @smoke @regression
  Scenario: Successful registration with valid data
    When I send a POST request to "/auth/register/" with body:
      """
       {
      "email": "john.doe+{time}@example.com",
      "first_name": "John",
      "last_name": "Doe",
      "password": "SecurePass123!",
      "password_confirm": "SecurePass123!"
    }
      """
    Then the response status should be 201
    And the response body should have field "id"
    And the response body field "email" should equal "john.doe+{time}@example.com"
    And the response body should NOT contain "password"

  # Email: missing
  @regression
  Scenario: Registration fails when email is missing
    When I send a POST request to "/auth/register/" with body:
      """
      {
        "first_name": "John", "last_name": "Doe",
        "password": "SecurePass123!",
        "password_confirm": "SecurePass123!"
      }
      """
    Then the response status should be 400
    And the response body field "email" should contain error "required"

  # Password: too short
  @regression
  Scenario: Registration fails when password is too short
    When I send a POST request to "/auth/register/" with body:
      """
      {
        "email": "test@example.com",
        "first_name": "John", "last_name": "Doe",
        "password": "Ab1!",
        "password_confirm": "Ab1!"
      }
      """
    Then the response status should be 400
    And the response body field "password" should contain error "8 characters"

  # Data-driven: invalid name characters
  @regression
  Scenario Outline: Registration fails with invalid name characters
    When I send a POST request to "/auth/register/" with body:
      """
      {
        "email": "test@example.com",
        "first_name": "<first_name>",
        "last_name": "<last_name>",
        "password": "SecurePass123!",
        "password_confirm": "SecurePass123!"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "<field>"

    Examples:
      | first_name | last_name | field      |
      | John123    | Doe       | first_name |
      | John@!     | Doe       | first_name |
      | John       | Doe99     | last_name  |
