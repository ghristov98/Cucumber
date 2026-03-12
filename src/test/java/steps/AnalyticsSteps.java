package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AnalyticsSteps {

    private final ScenarioContext ctx;

    public AnalyticsSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("the test has at least one submitted attempt")
    public void ensureSubmittedAttempt() {
        // Create question
        io.restassured.response.Response qResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("""
                {
                  "question_text": "Analytics test question",
                  "question_type": "multiple_choice",
                  "order": 1,
                  "answers": [
                    { "answer_text": "Correct", "is_correct": true,  "order": 1 },
                    { "answer_text": "Wrong",   "is_correct": false, "order": 2 }
                  ]
                }""")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/questions/");

        assertEquals("Failed to create analytics question: " + qResp.statusCode(),
                201, qResp.statusCode());
        ctx.createdQuestionId = qResp.jsonPath().getInt("id");
        ctx.createdAnswerId = qResp.jsonPath()
                .getList("answers", java.util.Map.class)
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.get("is_correct")))
                .mapToInt(a -> (Integer) a.get("id"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No correct answer found"));

        // Start attempt
        io.restassured.response.Response attemptResp = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{\"anonymous_name\": \"Analytics Taker\"}")
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/");

        assertEquals("Failed to start attempt: " + attemptResp.statusCode(),
                201, attemptResp.statusCode());
        ctx.createdAttemptId = attemptResp.jsonPath().getInt("id");

        ctx.verifiedPasswordCookie = attemptResp.getCookies()
                .entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);

        // Submit attempt
        var submitReq = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
            submitReq = submitReq.header("Cookie", ctx.verifiedPasswordCookie);
        }

        io.restassured.response.Response submitResp = submitReq
                .body(String.format("""
                {
                  "draft_answers": {
                    "%d": [%d]
                  }
                }""", ctx.createdQuestionId, ctx.createdAnswerId))
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/attempts/" + ctx.createdAttemptId + "/submit/");

        assertEquals("Failed to submit attempt: " + submitResp.statusCode(),
                200, submitResp.statusCode());
        System.out.println("Submitted attempt for analytics test");
    }

    // ── Requests ────────────────────────────────────────────────────────────

    @When("the author requests analytics for the test")
    public void authorGetsAnalytics() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/analytics/tests/" + ctx.createdTestSlug + "/");
    }

    @When("a non-author requests analytics for the test")
    public void nonAuthorGetsAnalytics() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/analytics/tests/" + ctx.createdTestSlug + "/");
    }

    @When("an unauthenticated user requests analytics for the test")
    public void unauthenticatedGetsAnalytics() {
        ctx.response = given()
                .header("Accept", "application/json")
                .when()
                .get("/analytics/tests/" + ctx.createdTestSlug + "/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the analytics response should contain required fields")
    public void verifyAnalyticsFields() {
        ctx.response.then()
                .body("total_attempts",   notNullValue())
                .body("average_score",    notNullValue())
                .body("pass_rate",        notNullValue())
                .body("completion_rate",  notNullValue())
                .body("question_stats",   notNullValue());
    }

    @Then("the analytics question stats should contain required fields")
    public void verifyQuestionStatsFields() {
        ctx.response.then()
                .body("question_stats[0].question_id",          notNullValue())
                .body("question_stats[0].question_text",        notNullValue())
                .body("question_stats[0].total_answered",       notNullValue())
                .body("question_stats[0].correct_count",        notNullValue())
                .body("question_stats[0].difficulty",           notNullValue())
                .body("question_stats[0].answer_distribution",  notNullValue());
    }

    @Then("the total attempts should be at least {int}")
    public void verifyTotalAttempts(int minAttempts) {
        int total = ctx.response.jsonPath().getInt("total_attempts");
        assertTrue("Expected at least " + minAttempts + " attempts but got: " + total,
                total >= minAttempts);
    }

    @Then("the completion rate should be between 0 and 100")
    public void verifyCompletionRate() {
        float rate = ctx.response.jsonPath().getFloat("completion_rate");
        assertTrue("Completion rate out of range: " + rate, rate >= 0 && rate <= 100);
    }

    @Then("the pass rate should be between 0 and 100")
    public void verifyPassRate() {
        float rate = ctx.response.jsonPath().getFloat("pass_rate");
        assertTrue("Pass rate out of range: " + rate, rate >= 0 && rate <= 100);
    }
}