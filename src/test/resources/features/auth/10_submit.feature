Feature: Submit Attempt - POST /tests/{slug}/attempts/{id}/submit/

  Background:
    Given the base URL is configured
    And a registered user exists with email "submitauthor@example.com" and password "SecurePass123!"
    And I log in with email "submitauthor@example.com" and password "SecurePass123!"

  @smoke @regression
  Scenario: Submit attempt without show_answers returns submitted status only
    Given I have a test created with title "No Answers Test"
    And the test has a multiple choice question with a known answer
    And I have started an anonymous attempt
    When I submit the attempt with correct answers
    Then the response status should be 200
    And the submit response should contain status submitted
    And the response body field "show_answers" should equal "false"

  @smoke @regression
  Scenario: Submit attempt with show_answers_after true returns results
    When I send an authenticated POST request to "/tests/" with body:
      """
      {
        "title": "Show Answers Test",
        "description": "Reveals answers after submit",
        "visibility": "link_only",
        "show_answers_after": true,
        "max_attempts": 3,
        "folder": null
      }
      """
    Then the response status should be 201
    And I save the test slug from response
    And the test has a multiple choice question with a known answer
    And I have started an anonymous attempt
    When I submit the attempt with correct answers
    Then the response status should be 200
    And the submit response should contain results with score

  @smoke @regression
  Scenario: Submit attempt without show_answers returns submitted status only
    Given I have a test created with title "No Answers Test" and show answers disabled
    And the test has a multiple choice question with a known answer
    And I have started an anonymous attempt
    When I submit the attempt with correct answers
    Then the response status should be 200
    And the submit response should contain status submitted
    And the response body field "show_answers" should equal "false"

  @regression
  Scenario: Submit attempt with empty body uses saved drafts
    Given I have a test created with title "Draft Submit Test"
    And the test has a multiple choice question with a known answer
    And I have started an anonymous attempt
    And I save draft answers for the attempt
    When I submit the attempt with empty body
    Then the response status should be 200
    And the submit response should contain status submitted

  @regression
  Scenario: Non-owner cannot submit the attempt
    Given I have a test created with title "Ownership Submit Test"
    And the test has a multiple choice question with a known answer
    And I have started an anonymous attempt
    When a different user tries to submit the attempt
    Then the permission error should be returned