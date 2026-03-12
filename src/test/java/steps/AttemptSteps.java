package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AttemptSteps {

    private final ScenarioContext ctx;

    public AttemptSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("the test has a multiple choice question with a known answer")
    public void createQuestionWithKnownAnswer() {
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

        assertEquals("Failed to create question: " + r.statusCode(), 201, r.statusCode());
        ctx.createdQuestionId = r.jsonPath().getInt("id");

        // Save the correct answer ID
        ctx.createdAnswerId = r.jsonPath()
                .getList("answers", java.util.Map.class)
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.get("is_correct")))
                .mapToInt(a -> (Integer) a.get("id"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No correct answer found"));

        System.out.println("Question ID: " + ctx.createdQuestionId + ", Correct Answer ID: " + ctx.createdAnswerId);
    }

    @Given("I have started an anonymous attempt")
    public void startAnonymousAttempt() {
        var request = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        // If we already have a session cookie, reuse it
        if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
            request = request.header("Cookie", ctx.verifiedPasswordCookie);
        }

        io.restassured.response.Response r = request
                .body("{\"anonymous_name\": \"Test Taker\"}")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/");

        assertEquals("Failed to start attempt: " + r.statusCode(), 201, r.statusCode());
        ctx.createdAttemptId = r.jsonPath().getInt("id");

        // Always capture/update the session cookie
        String cookies = r.getCookies()
                .entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);

        if (!cookies.isEmpty()) {
            ctx.verifiedPasswordCookie = cookies;
        }

        System.out.println("Attempt ID: " + ctx.createdAttemptId);
        System.out.println("Session cookie: " + ctx.verifiedPasswordCookie);
    }

    @Given("I have started an authenticated attempt")
    public void startAuthenticatedAttempt() {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("{}")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/");

        assertEquals("Failed to start attempt: " + r.statusCode(), 201, r.statusCode());
        ctx.createdAttemptId = r.jsonPath().getInt("id");
    }

    @Given("I have a test created with max attempts of {int}")
    public void createTestWithMaxAttempts(int maxAttempts) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
            {
              "title": "Max Attempts Test",
              "description": "Test for max attempts",
              "visibility": "link_only",
              "max_attempts": %d,
              "show_answers_after": false,
              "folder": null
            }""", maxAttempts))
                .when()
                .post("/tests/");

        assertEquals("Failed to create test: " + r.statusCode(), 201, r.statusCode());
        ctx.createdTestSlug = r.jsonPath().getString("slug");
        assertNotNull("Slug was null", ctx.createdTestSlug);
    }

    // ── POST /attempts/ ─────────────────────────────────────────────────────

    @When("an anonymous user starts an attempt with name {string}")
    public void startAnonymousAttemptWithName(String name) {
        ctx.response = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("{\"anonymous_name\": \"%s\"}", name))
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/");

        if (ctx.response.statusCode() == 201) {
            ctx.createdAttemptId = ctx.response.jsonPath().getInt("id");

            // Capture session cookie
            ctx.verifiedPasswordCookie = ctx.response.getCookies()
                    .entrySet()
                    .stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);
        }
    }

    @When("an authenticated user starts an attempt")
    public void startAttemptAuthenticated() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("{}")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/");

        if (ctx.response.statusCode() == 201) {
            ctx.createdAttemptId = ctx.response.jsonPath().getInt("id");
        }
    }

    @When("the user starts {int} more attempts to exceed the limit")
    public void exceedAttemptLimit(int extraAttempts) {
        for (int i = 0; i < extraAttempts; i++) {
            var request = given()
                    .filter(ctx.sessionFilter)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            // Send same session cookie so server recognises it as the same user
            if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
                request = request.header("Cookie", ctx.verifiedPasswordCookie);
            }

            ctx.response = request
                    .body("{\"anonymous_name\": \"Taker\"}")
                    .when()
                    .post("/tests/" + ctx.createdTestSlug + "/attempts/");

            System.out.println("Extra attempt " + (i + 1) + " status: " + ctx.response.statusCode());
        }
    }

    // ── PATCH /attempts/{id}/ ───────────────────────────────────────────────

    @When("I save draft answers for the attempt")
    public void saveDraftAnswers() {
        var request = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        // Send session cookie manually to prove ownership
        if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
            request = request.header("Cookie", ctx.verifiedPasswordCookie);
        }

        ctx.response = request
                .body(String.format("""
            {
              "draft_answers": {
                "%d": [%d]
              }
            }""", ctx.createdQuestionId, ctx.createdAnswerId))
                .when()
                .patch("/tests/" + ctx.createdTestSlug + "/attempts/" + ctx.createdAttemptId + "/");
    }

    @When("a different user tries to patch the attempt")
    public void differentUserPatchesAttempt() {
        // No session filter = different session = not the owner
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "draft_answers": { "%d": [%d] }
                }""", ctx.createdQuestionId, ctx.createdAnswerId))
                .when()
                .patch("/tests/" + ctx.createdTestSlug + "/attempts/" + ctx.createdAttemptId + "/");
    }

    // ── POST /attempts/{id}/submit/ ─────────────────────────────────────────

    @When("I submit the attempt with correct answers")
    public void submitAttemptWithCorrectAnswers() {
        var request = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
            request = request.header("Cookie", ctx.verifiedPasswordCookie);
        }

        ctx.response = request
                .body(String.format("""
            {
              "draft_answers": {
                "%d": [%d]
              }
            }""", ctx.createdQuestionId, ctx.createdAnswerId))
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/" + ctx.createdAttemptId + "/submit/");
    }

    @When("I submit the attempt with empty body")
    public void submitAttemptWithEmptyBody() {
        var request = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
            request = request.header("Cookie", ctx.verifiedPasswordCookie);
        }

        ctx.response = request
                .body("{}")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/" + ctx.createdAttemptId + "/submit/");
    }

    @When("a different user tries to submit the attempt")
    public void differentUserSubmitsAttempt() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{}")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/" + ctx.createdAttemptId + "/submit/");
    }

    // ── GET /results/ ───────────────────────────────────────────────────────

    @When("the author requests all results for the test")
    public void authorGetsAllResults() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/results/");
    }

    @When("the author requests result details for the attempt")
    public void authorGetsResultDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/results/" + ctx.createdAttemptId + "/");
    }

    @When("an unauthenticated user requests all results for the test")
    public void unauthenticatedUserGetsResults() {
        ctx.response = given()
                .header("Accept", "application/json")
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/results/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the attempt response should contain required fields")
    public void verifyAttemptFields() {
        ctx.response.then()
                .body("id",             notNullValue())
                .body("anonymous_name", notNullValue())
                .body("attempt_number", notNullValue())
                .body("started_at",     notNullValue());
    }

    @Then("the attempt should be rejected with max attempts error")
    public void verifyMaxAttemptsError() {
        ctx.response.then()
                .statusCode(403)
                .body("error", containsString("Maximum attempts reached"));
    }

    @Then("the draft answers should be saved")
    public void verifyDraftAnswersSaved() {
        ctx.response.then()
                .body("draft_answers", notNullValue());
    }

    @Then("the submit response should contain status submitted")
    public void verifySubmitStatus() {
        ctx.response.then()
                .body("status",  equalTo("submitted"))
                .body("message", notNullValue());
    }

    @Then("the submit response should contain results with score")
    public void verifySubmitResultsWithScore() {
        ctx.response.then()
                .body("status",                  equalTo("submitted"))
                .body("show_answers",            equalTo(true))
                .body("results.score",           notNullValue())
                .body("results.total_questions", notNullValue())
                .body("results.correct_answers", notNullValue());
    }

    @Then("the permission error should be returned")
    public void verifyPermissionError() {
        ctx.response.then()
                .statusCode(403)
                .body("detail", containsString("permission"));
    }

    @Then("the results list should contain required fields")
    public void verifyResultsListFields() {
        ctx.response.then()
                .body("[0].id",             notNullValue())
                .body("[0].attempt_number", notNullValue())
                .body("[0].started_at",     notNullValue())
                .body("[0].score",          notNullValue());
    }

    @Then("the result detail should contain answers breakdown")
    public void verifyResultDetailFields() {
        ctx.response.then()
                .body("id",              notNullValue())
                .body("score",           notNullValue())
                .body("answers",         notNullValue())
                .body("answers[0].question_id",   notNullValue())
                .body("answers[0].is_correct",    notNullValue());
    }
}