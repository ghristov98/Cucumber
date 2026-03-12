package steps;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;
import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;

public class QuestionSteps {

    private final ScenarioContext ctx;

    public QuestionSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("I have a multiple choice question created for the test")
    public void createMultipleChoiceQuestion() {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("""
                {
                  "question_text": "What is 2+2?",
                  "question_type": "multiple_choice",
                  "order": 1,
                  "answers": [
                    { "answer_text": "3", "is_correct": false, "order": 1 },
                    { "answer_text": "4", "is_correct": true,  "order": 2 },
                    { "answer_text": "5", "is_correct": false, "order": 3 }
                  ]
                }""")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/questions/");

        assertEquals("Failed to create question, status: " + r.statusCode(), 201, r.statusCode());
        ctx.createdQuestionId = r.jsonPath().getInt("id");
        System.out.println("Created question ID: " + ctx.createdQuestionId);
    }

    @Given("I have an exact answer question created for the test")
    public void createExactAnswerQuestion() {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("""
                {
                  "question_text": "When was Bulgaria formed? (Enter the year)",
                  "question_type": "exact_answer",
                  "order": 1,
                  "correct_answer": "681"
                }""")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/questions/");

        assertEquals("Failed to create exact answer question, status: " + r.statusCode(), 201, r.statusCode());
        ctx.createdQuestionId = r.jsonPath().getInt("id");
    }

    @Given("I have a second question created for the test")
    public void createSecondQuestion() {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("""
                {
                  "question_text": "What is 3+3?",
                  "question_type": "multiple_choice",
                  "order": 2,
                  "answers": [
                    { "answer_text": "5", "is_correct": false, "order": 1 },
                    { "answer_text": "6", "is_correct": true,  "order": 2 }
                  ]
                }""")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/questions/");

        assertEquals("Failed to create second question, status: " + r.statusCode(), 201, r.statusCode());
        ctx.secondQuestionId = r.jsonPath().getInt("id");
    }

    // ── Requests ────────────────────────────────────────────────────────────

    @When("I send an authenticated POST request to the questions endpoint with body:")
    public void postQuestion(String body) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(body)
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/questions/");
    }

    @When("I send an authenticated GET request to the created question")
    public void getCreatedQuestion() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/questions/" + ctx.createdQuestionId + "/");
    }

    @When("I send an authenticated PATCH request to the created question with body:")
    public void patchCreatedQuestion(String body) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(body)
                .when()
                .patch("/tests/" + ctx.createdTestSlug + "/questions/" + ctx.createdQuestionId + "/");
    }

    @When("I send an authenticated DELETE request to the created question")
    public void deleteCreatedQuestion() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/tests/" + ctx.createdTestSlug + "/questions/" + ctx.createdQuestionId + "/");
    }

    @When("I send a reorder request with the two questions swapped")
    public void reorderQuestions() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "order": [
                    { "id": %d, "order": 2 },
                    { "id": %d, "order": 1 }
                  ]
                }""", ctx.createdQuestionId, ctx.secondQuestionId))
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/questions/reorder/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the question response should contain required fields")
    public void verifyQuestionFields() {
        ctx.response.then()
                .body("id",            org.hamcrest.Matchers.notNullValue())
                .body("question_text", org.hamcrest.Matchers.notNullValue())
                .body("question_type", org.hamcrest.Matchers.notNullValue())
                .body("order",         org.hamcrest.Matchers.notNullValue())
                .body("answers",       org.hamcrest.Matchers.notNullValue());
    }

    @Then("the exact answer question should contain correct_answer field")
    public void verifyExactAnswerField() {
        ctx.response.then()
                .body("correct_answer", org.hamcrest.Matchers.notNullValue())
                .body("answers",        org.hamcrest.Matchers.hasSize(0));
    }
    @Then("the response should contain exact answer validation error")
    public void verifyExactAnswerValidationError() {
        ctx.response.then()
                .body("correct_answer", not(emptyOrNullString()))
                .body("answers", hasSize(0));
    }

    @Then("the reorder response should return status ok")
    public void verifyReorderResponse() {
        ctx.response.then()
                .body("status", org.hamcrest.Matchers.equalTo("ok"));
    }

    @Then("the deleted question should no longer be accessible")
    public void verifyQuestionDeleted() {
        io.restassured.response.Response r = given()
                .header("Authorization", "Bearer " + ctx.accessToken)
                .get("/tests/" + ctx.createdTestSlug + "/questions/" + ctx.createdQuestionId + "/");
        assertEquals("Expected 404 after delete but got: " + r.statusCode(), 404, r.statusCode());
    }
}