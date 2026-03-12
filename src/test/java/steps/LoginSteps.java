package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;

public class LoginSteps {

    private final ScenarioContext ctx;

    public LoginSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("a registered user exists with email {string} and password {string}")
    public void ensureUserExists(String email, String password) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Test",
                  "last_name": "User",
                  "password": "%s",
                  "password_confirm": "%s"
                }""", email, password, password))
                .post("/auth/register/");
        // Save tokens for use in subsequent steps
        ctx.refreshToken = ctx.response.jsonPath().getString("refresh");
        ctx.accessToken  = ctx.response.jsonPath().getString("access");
    }

    @Then("the {string} token should be a valid JWT format")
    public void verifyJwtFormat(String tokenField) {
        String body = ctx.response.getBody().asString();
        assertNotNull("Response body is null", body);
        assertFalse("Response body is empty", body.isEmpty());

        String token = ctx.response.jsonPath().getString(tokenField);
        assertNotNull("Token '" + tokenField + "' not found in response: " + body, token);

        String[] parts = token.split("\\.");
        assertEquals(
                "Token '" + tokenField + "' is not a valid JWT. Got: " + token,
                3, parts.length
        );
    }

    @Then("the response body should contain an authentication error")
    public void verifyAuthError() {
        String body = ctx.response.getBody().asString();
        assertTrue(
                "Expected auth error in body but got: " + body,
                body.contains("detail") || body.contains("non_field_errors")
        );
    }
}