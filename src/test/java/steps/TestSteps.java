package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;

public class TestSteps {

    private final ScenarioContext ctx;

    public TestSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Create a test and save its slug ─────────────────────────────────────

    @Given("I have a test created with title {string}")
    public void createTestAndSaveSlug(String title) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
            {
              "title": "%s",
              "description": "Test description",
              "visibility": "link_only",
              "show_answers_after": false,
              "time_limit_minutes": 30,
              "max_attempts": 3,
              "folder": null
            }""", title))
                .when()
                .post("/tests/");

        assertEquals("Failed to create test, status: " + r.statusCode(), 201, r.statusCode());
        ctx.createdTestSlug = r.jsonPath().getString("slug");
        assertNotNull("Slug was null after creating test", ctx.createdTestSlug);
    }

    @Given("I have a password protected test with title {string} and password {string}")
    public void createPasswordProtectedTest(String title, String password) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "title": "%s",
                  "description": "Protected test",
                  "visibility": "password_protected",
                  "password": "%s",
                  "time_limit_minutes": 30,
                  "max_attempts": 30,
                  "show_answers_after": false,
                  "folder": null
                }""", title, password))
                .when()
                .post("/tests/");

        assertEquals("Failed to create protected test, status: " + r.statusCode(), 201, r.statusCode());
        ctx.createdTestSlug = r.jsonPath().getString("slug");
    }

    @Given("I have a test created with title {string} and show answers disabled")
    public void createTestWithShowAnswersDisabled(String title) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
            {
              "title": "%s",
              "description": "Test description",
              "visibility": "link_only",
              "show_answers_after": false,
              "max_attempts": 3,
              "folder": null
            }""", title))
                .when()
                .post("/tests/");

        assertEquals("Failed to create test, status: " + r.statusCode(), 201, r.statusCode());
        ctx.createdTestSlug = r.jsonPath().getString("slug");
        assertNotNull("Slug was null after creating test", ctx.createdTestSlug);
    }

    // ── Slug-based requests ─────────────────────────────────────────────────

    @When("I send an authenticated GET request to the created test")
    public void getCreatedTest() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/");
    }

    @When("I send an authenticated PATCH request to the created test with body:")
    public void patchCreatedTest(String body) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(body)
                .when()
                .patch("/tests/" + ctx.createdTestSlug + "/");
    }

    @When("I send an authenticated DELETE request to the created test")
    public void deleteCreatedTest() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/tests/" + ctx.createdTestSlug + "/");
    }

    @When("I send an unauthenticated GET request to take the created test")
    public void takeCreatedTestUnauthenticated() {
        ctx.response = given()
                .header("Accept", "application/json")
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/take/");
    }

    @When("I send a GET request to take the created test with verified password header")
    public void takeCreatedTestWithPasswordHeader() {
        var request = given()
                .filter(ctx.sessionFilter)
                .header("Accept", "application/json")
                .header("X-Test-Password-Verified", "true");

        // Also send the cookie manually if we captured one
        if (ctx.verifiedPasswordCookie != null && !ctx.verifiedPasswordCookie.isEmpty()) {
            request = request.header("Cookie", ctx.verifiedPasswordCookie);
        }

        ctx.response = request
                .when()
                .get("/tests/" + ctx.createdTestSlug + "/take/");

        // Print response for debugging
        System.out.println("Take test response: " + ctx.response.getBody().asString());
    }

    @When("I verify the password {string} for the created test")
    public void verifyTestPassword(String password) {
        ctx.response = given()
                .filter(ctx.sessionFilter)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("{\"password\": \"%s\"}", password))
                .when()
                .post("/tests/" + ctx.createdTestSlug + "/verify-password/");

        // Print all cookies so we can see what the server sends back
        System.out.println("Cookies after verify: " + ctx.response.getCookies());

        // Try to capture any session-related cookie
        ctx.verifiedPasswordCookie = ctx.response.getCookies()
                .entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);

        System.out.println("Captured cookie: " + ctx.verifiedPasswordCookie);
    }

    // ── Response assertions ─────────────────────────────────────────────────

    @Then("the response should be a list")
    public void verifyResponseIsList() {
        String body = ctx.response.getBody().asString();
        assertTrue("Expected a JSON array but got: " + body, body.trim().startsWith("["));
    }

    @Then("the test response should contain required list fields")
    public void verifyTestListFields() {
        ctx.response.then()
                .body("[0].id", org.hamcrest.Matchers.notNullValue())
                .body("[0].title", org.hamcrest.Matchers.notNullValue())
                .body("[0].slug", org.hamcrest.Matchers.notNullValue())
                .body("[0].visibility", org.hamcrest.Matchers.notNullValue())
                .body("[0].created_at", org.hamcrest.Matchers.notNullValue());
    }

    @Then("the test response should contain required detail fields")
    public void verifyTestDetailFields() {
        ctx.response.then()
                .body("id", org.hamcrest.Matchers.notNullValue())
                .body("title", org.hamcrest.Matchers.notNullValue())
                .body("slug", org.hamcrest.Matchers.notNullValue())
                .body("visibility", org.hamcrest.Matchers.notNullValue())
                .body("questions", org.hamcrest.Matchers.notNullValue());
    }

    @Then("the take test response should not contain is_correct field")
    public void verifyNoIsCorrectField() {
        String body = ctx.response.getBody().asString();
        assertFalse("is_correct field was exposed in take response: " + body,
                body.contains("is_correct"));
    }

    @Then("the response should require a password")
    public void verifyPasswordRequired() {
        ctx.response.then()
                .body("requires_password", org.hamcrest.Matchers.equalTo(true));
    }

    @Then("the password verification should succeed")
    public void verifyPasswordVerificationSuccess() {
        ctx.response.then()
                .body("verified", org.hamcrest.Matchers.equalTo(true))
                .body("token", org.hamcrest.Matchers.equalTo("verified"));
    }

    @Then("the deleted test should no longer be accessible")
    public void verifyTestDeleted() {
        io.restassured.response.Response r = given()
                .header("Authorization", "Bearer " + ctx.accessToken)
                .get("/api/tests/" + ctx.createdTestSlug + "/");
        assertEquals("Expected 404 after delete but got: " + r.statusCode(), 404, r.statusCode());
    }

    @Then("I save the test slug from response")
    public void saveTestSlugFromResponse() {
        ctx.createdTestSlug = ctx.response.jsonPath().getString("slug");
        assertNotNull("Slug was null in response", ctx.createdTestSlug);
        System.out.println("Saved slug: " + ctx.createdTestSlug);
    }
}