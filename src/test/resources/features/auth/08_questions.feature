Feature: Questions - POST, GET, PATCH, DELETE /tests/{slug}/questions/

  Background:
    Given the base URL is configured
    And a registered user exists with email "questionauthor@example.com" and password "SecurePass123!"
    And I log in with email "questionauthor@example.com" and password "SecurePass123!"
    And I have a test created with title "Question Test"

  # ─────────────────────────────────────────────
  # POST /tests/{slug}/questions/ — Create question
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Successfully create a multiple_choice question
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "What is 2+2?",
        "question_type": "multiple_choice",
        "order": 1,
        "answers": [
          { "answer_text": "3", "is_correct": false, "order": 1 },
          { "answer_text": "4", "is_correct": true,  "order": 2 },
          { "answer_text": "5", "is_correct": false, "order": 3 }
        ]
      }
      """
    Then the response status should be 201
    And the question response should contain required fields
    And the response body field "question_type" should equal "multiple_choice"

  @smoke @regression
  Scenario: Successfully create a multi_select question
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Which are prime numbers?",
        "question_type": "multi_select",
        "order": 1,
        "answers": [
          { "answer_text": "2", "is_correct": true,  "order": 1 },
          { "answer_text": "3", "is_correct": true,  "order": 2 },
          { "answer_text": "4", "is_correct": false, "order": 3 }
        ]
      }
      """
    Then the response status should be 201
    And the question response should contain required fields
    And the response body field "question_type" should equal "multi_select"

  @smoke @regression
  Scenario: Successfully create an exact_answer question
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "When was Bulgaria formed? (Enter the year)",
        "question_type": "exact_answer",
        "order": 1,
        "correct_answer": "681"
      }
      """
    Then the response status should be 201
    And the exact answer question should contain correct_answer field
    And the response body field "question_type" should equal "exact_answer"
    And the response body field "correct_answer" should equal "681"

  @regression
  Scenario: Create question fails when question_text is missing
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_type": "multiple_choice",
        "order": 1,
        "answers": [
          { "answer_text": "Yes", "is_correct": true,  "order": 1 },
          { "answer_text": "No",  "is_correct": false, "order": 2 }
        ]
      }
      """
    Then the response status should be 400
    And the response body field "question_text" should contain error "required"

  @regression
  Scenario: Create question fails with invalid question_type
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Is this valid?",
        "question_type": "true_false",
        "order": 1
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "question_type"

  @regression
  Scenario: Create multiple_choice question fails with less than 2 answers
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Only one answer?",
        "question_type": "multiple_choice",
        "order": 1,
        "answers": [
          { "answer_text": "Only", "is_correct": true, "order": 1 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "answers"

  @regression
  Scenario: Create multiple_choice question fails with no correct answer
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "No correct answer?",
        "question_type": "multiple_choice",
        "order": 1,
        "answers": [
          { "answer_text": "A", "is_correct": false, "order": 1 },
          { "answer_text": "B", "is_correct": false, "order": 2 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "answers"

  @regression
  Scenario: Create multiple_choice question fails with more than one correct answer
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Too many correct?",
        "question_type": "multiple_choice",
        "order": 1,
        "answers": [
          { "answer_text": "A", "is_correct": true, "order": 1 },
          { "answer_text": "B", "is_correct": true, "order": 2 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "answers"

  @regression
  Scenario: Create multiple_choice question fails with duplicate answer orders
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Duplicate orders?",
        "question_type": "multiple_choice",
        "order": 1,
        "answers": [
          { "answer_text": "A", "is_correct": true,  "order": 1 },
          { "answer_text": "B", "is_correct": false, "order": 1 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "answers"

  @regression
  Scenario: Create multi_select question fails with no correct answers
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "No correct at all?",
        "question_type": "multi_select",
        "order": 1,
        "answers": [
          { "answer_text": "A", "is_correct": false, "order": 1 },
          { "answer_text": "B", "is_correct": false, "order": 2 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "answers"

  @regression
  Scenario: Create exact_answer question fails when correct_answer is missing
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Missing correct answer?",
        "question_type": "exact_answer",
        "order": 1
      }
      """
    Then the response status should be 400
    And the response body field "correct_answer" should contain error "correct_answer"

  @regression
  Scenario: Create exact_answer question fails when correct_answer exceeds 30 characters
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Too long correct answer?",
        "question_type": "exact_answer",
        "order": 1,
        "correct_answer": "This answer is way too long to be valid here!"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "correct_answer"

  @regression
  Scenario: Create exact_answer question fails when answers array is provided
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "Exact with answers?",
        "question_type": "exact_answer",
        "order": 1,
        "correct_answer": "681",
        "answers": [
          { "answer_text": "A", "is_correct": true, "order": 1 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "answers"

  @regression
  Scenario: Create question fails when MC type includes correct_answer
    When I send an authenticated POST request to the questions endpoint with body:
      """
      {
        "question_text": "MC with correct_answer field?",
        "question_type": "multiple_choice",
        "order": 1,
        "correct_answer": "some text",
        "answers": [
          { "answer_text": "A", "is_correct": true,  "order": 1 },
          { "answer_text": "B", "is_correct": false, "order": 2 }
        ]
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "correct_answer"

  @regression
  Scenario: Create question fails without authentication
    When I send an unauthenticated POST request to "/tests/some-fake-slug/questions/" with body:
      """
      {
        "question_text": "Unauthorized question?",
        "question_type": "multiple_choice",
        "order": 1
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # GET /tests/{slug}/questions/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can get a question by ID
    Given I have a multiple choice question created for the test
    When I send an authenticated GET request to the created question
    Then the response status should be 200
    And the question response should contain required fields

  @regression
  Scenario: Get question fails without authentication
    Given I have a multiple choice question created for the test
    When I send an unauthenticated GET request to "/tests/some-fake-slug/questions/1/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # PATCH /tests/{slug}/questions/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can update question text
    Given I have a multiple choice question created for the test
    When I send an authenticated PATCH request to the created question with body:
      """
      {
        "question_text": "Updated question text?"
      }
      """
    Then the response status should be 200
    And the profile field "question_text" should equal "Updated question text?"

  @regression
  Scenario: Author can update question answers
    Given I have a multiple choice question created for the test
    When I send an authenticated PATCH request to the created question with body:
      """
      {
        "answers": [
          { "answer_text": "New A", "is_correct": true,  "order": 1 },
          { "answer_text": "New B", "is_correct": false, "order": 2 }
        ]
      }
      """
    Then the response status should be 200
    And the question response should contain required fields

  @regression
  Scenario: Update question fails with invalid question_type
    Given I have a multiple choice question created for the test
    When I send an authenticated PATCH request to the created question with body:
      """
      {
        "question_type": "invalid_type"
      }
      """
    Then the response status should be 400
    And the response body should contain a field error for "question_type"

  @regression
  Scenario: Update question fails without authentication
    Given I have a multiple choice question created for the test
    When I send an unauthenticated PATCH request to "/tests/some-fake-slug/questions/1/" with body:
      """
      {
        "question_text": "Hacked?"
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # DELETE /tests/{slug}/questions/{id}/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can delete a question
    Given I have a multiple choice question created for the test
    When I send an authenticated DELETE request to the created question
    Then the response status should be 204
    And the response body should be empty
    And the deleted question should no longer be accessible

  @regression
  Scenario: Delete question fails without authentication
    Given I have a multiple choice question created for the test
    When I send an unauthenticated DELETE request to "/tests/some-fake-slug/questions/1/"
    Then the response status should be 401
    And the response body should contain an authentication error

  # ─────────────────────────────────────────────
  # POST /tests/{slug}/questions/reorder/
  # ─────────────────────────────────────────────

  @smoke @regression
  Scenario: Author can reorder questions
    Given I have a multiple choice question created for the test
    And I have a second question created for the test
    When I send a reorder request with the two questions swapped
    Then the response status should be 200
    And the reorder response should return status ok

  @regression
  Scenario: Reorder fails without authentication
    When I send an unauthenticated POST request to "/tests/some-fake-slug/questions/reorder/" with body:
      """
      {
        "order": [
          { "id": 1, "order": 1 }
        ]
      }
      """
    Then the response status should be 401
    And the response body should contain an authentication error