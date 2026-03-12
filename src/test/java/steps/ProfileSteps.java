package steps;

import io.cucumber.java.en.*;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class ProfileSteps {

    private final ScenarioContext ctx;

    public ProfileSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }
    @When("I send a GET request with invalid token to {string}")
    public void sendGetWithInvalidToken(String endpoint) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer this.is.not.valid")
                .when()
                .get(endpoint);
    }
    @Then("the response body should contain the user profile")
    public void verifyUserProfile() {
        String body = ctx.response.getBody().asString();
        assertTrue("Missing 'id' in profile: "         + body, body.contains("id"));
        assertTrue("Missing 'email' in profile: "      + body, body.contains("email"));
        assertTrue("Missing 'first_name' in profile: " + body, body.contains("first_name"));
        assertTrue("Missing 'last_name' in profile: "  + body, body.contains("last_name"));
        assertTrue("Missing 'created_at' in profile: " + body, body.contains("created_at"));
    }

    @Then("the response body should NOT contain sensitive fields")
    public void verifyNoSensitiveFields() {
        String body = ctx.response.getBody().asString();
        assertFalse("Password leaked in response: " + body, body.contains("password"));
    }

    @Then("the profile field {string} should equal {string}")
    public void verifyProfileField(String field, String expectedValue) {
        String actual = ctx.response.jsonPath().getString(field);
        assertEquals(
                "Expected '" + field + "' to be '" + expectedValue + "' but was '" + actual + "'",
                expectedValue, actual
        );
    }

    @Then("the response body should be empty")
    public void verifyEmptyBody() {
        String body = ctx.response.getBody().asString();
        assertTrue("Expected empty body but got: " + body, body == null || body.trim().isEmpty());
    }
}