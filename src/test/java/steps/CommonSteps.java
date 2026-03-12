package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CommonSteps {

    private final ScenarioContext ctx;

    public CommonSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Base URL ────────────────────────────────────────────────────────────

    @Given("the base URL is configured")
    public void configureBaseUrl() {
        baseURI = "https://exampractices.com/api"; // ← your URL here
    }

    // ── Unauthenticated requests ────────────────────────────────────────────

    @When("I send a POST request to {string} with body:")
    public void sendPostRequest(String endpoint, String body) {
        String processedBody = body.replace("{time}",
                String.valueOf(System.currentTimeMillis()));
        ctx.lastProcessedBody = processedBody;
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(processedBody)
                .when()
                .post(endpoint);
    }

    @When("I send an unauthenticated GET request to {string}")
    public void sendUnauthenticatedGetRequest(String endpoint) {
        String resolvedEndpoint = endpoint
                .replace("{id}",         String.valueOf(ctx.createdCompanyId))
                .replace("{company_id}", String.valueOf(ctx.createdCompanyId))
                .replace("{slug}",       ctx.createdTestSlug != null ? ctx.createdTestSlug : "");

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .get(resolvedEndpoint);
    }

    @When("I send an unauthenticated PATCH request to {string} with body:")
    public void sendUnauthenticatedPatchRequest(String endpoint, String body) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(body)
                .when()
                .patch(endpoint);
    }

    @When("I send an unauthenticated DELETE request to {string}")
    public void sendUnauthenticatedDeleteRequest(String endpoint) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .delete(endpoint);
    }

    // ── Authenticated requests ──────────────────────────────────────────────

    @When("I send an authenticated GET request to {string}")
    public void sendAuthenticatedGetRequest(String endpoint) {
        assertNotNull("Access token is null — login step may have failed", ctx.accessToken);
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get(endpoint);
    }

    @When("I send an authenticated POST request to {string} with body:")
    public void sendAuthenticatedPostRequest(String endpoint, String body) {
        assertNotNull("Access token is null — login step may have failed", ctx.accessToken);

        // Replace placeholders with actual IDs
        String resolvedEndpoint = endpoint
                .replace("{id}",         String.valueOf(ctx.createdCompanyId))
                .replace("{company_id}", String.valueOf(ctx.createdCompanyId))
                .replace("{slug}",       ctx.createdTestSlug != null ? ctx.createdTestSlug : "");

        String processedBody = body.replace("{time}",
                String.valueOf(System.currentTimeMillis()));
        ctx.lastProcessedBody = processedBody;

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(processedBody)
                .when()
                .post(resolvedEndpoint);
    }

    @When("I send an authenticated DELETE request to {string}")
    public void sendAuthenticatedDeleteRequest(String endpoint) {
        assertNotNull("Access token is null — login step may have failed", ctx.accessToken);
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete(endpoint);
    }

    @When("I send an unauthenticated POST request to {string} with body:")
    public void sendUnauthenticatedPostRequest(String endpoint, String body) {
        String resolvedEndpoint = endpoint
                .replace("{id}",         String.valueOf(ctx.createdCompanyId))
                .replace("{company_id}", String.valueOf(ctx.createdCompanyId))
                .replace("{slug}",       ctx.createdTestSlug != null ? ctx.createdTestSlug : "");

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(body)
                .when()
                .post(resolvedEndpoint);
    }

    @When("I send an authenticated PATCH request to {string} with body:")
    public void sendAuthenticatedPatchRequest(String endpoint, String body) {
        assertNotNull("Access token is null — login step may have failed", ctx.accessToken);

        String resolvedEndpoint = endpoint
                .replace("{id}",         String.valueOf(ctx.createdCompanyId))
                .replace("{company_id}", String.valueOf(ctx.createdCompanyId))
                .replace("{slug}",       ctx.createdTestSlug != null ? ctx.createdTestSlug : "");

        String processedBody = body.replace("{time}",
                String.valueOf(System.currentTimeMillis()));
        ctx.lastProcessedBody = processedBody;

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(processedBody)
                .when()
                .patch(resolvedEndpoint);
    }


    // ── Status & body assertions ────────────────────────────────────────────

    @Then("the response status should be {int}")
    public void verifyStatus(int status) {
        ctx.response.then().statusCode(status);
    }

    @Then("the response body should have field {string}")
    public void verifyFieldExists(String field) {
        ctx.response.then().body(field, notNullValue());
    }

    @Then("the response body field {string} should equal {string}")
    public void verifyFieldValue(String field, String expectedValue) {
        String processedExpected = expectedValue.replace("{time}",
                extractTimeFromBody(ctx.lastProcessedBody));

        // Check what type the field actually is in the response
        Object actualValue = ctx.response.jsonPath().get(field);

        if (actualValue instanceof Integer) {
            ctx.response.then().body(field, equalTo(Integer.parseInt(processedExpected)));
        } else if (actualValue instanceof Float || actualValue instanceof Double) {
            ctx.response.then().body(field, equalTo(Float.parseFloat(processedExpected)));
        } else if (actualValue instanceof Boolean) {
            ctx.response.then().body(field, equalTo(Boolean.parseBoolean(processedExpected)));
        } else {
            // String — compare as-is
            ctx.response.then().body(field, equalTo(processedExpected));
        }
    }

    @Then("the response body should NOT contain {string}")
    public void verifyFieldAbsent(String field) {
        ctx.response.then().body("$", not(hasKey(field)));
    }

    @Then("the response body field {string} should contain error {string}")
    public void verifyFieldError(String field, String errorFragment) {
        String errors = ctx.response.jsonPath()
                .getList(field, String.class).toString();
        assertTrue(
                "Expected error with '" + errorFragment + "' but got: " + errors,
                errors.toLowerCase().contains(errorFragment.toLowerCase())
        );
    }

    @Then("the response body should contain a field error for {string}")
    public void verifyFieldHasAnyError(String field) {
        ctx.response.then().body(field, notNullValue());
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private String extractTimeFromBody(String body) {
        if (body == null) return "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\+(\\d+)@")
                .matcher(body);
        return matcher.find() ? matcher.group(1) : "";
    }
}