package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;

public class RefreshSteps {

    private final ScenarioContext ctx;

    public RefreshSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("I log in with email {string} and password {string}")
    public void loginAndSaveTokens(String email, String password) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
            {
              "email": "%s",
              "password": "%s"
            }""", email, password))
                .when()
                .post("/auth/login/");

        ctx.refreshToken = ctx.response.jsonPath().getString("refresh");
        ctx.accessToken  = ctx.response.jsonPath().getString("access");
    }

    @When("I send the saved refresh token to {string}")
    public void sendSavedRefreshToken(String endpoint) {
        assertNotNull("Refresh token is null — login step may have failed", ctx.refreshToken);

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "refresh": "%s"
                }""", ctx.refreshToken))
                .when()
                .post(endpoint);
    }

    @Then("the new access token should be different from the original")
    public void verifyNewAccessToken() {
        String newAccessToken = ctx.response.jsonPath().getString("access");
        assertNotNull("New access token is null", newAccessToken);
        assertNotEquals(
                "Expected a new access token but got the same one",
                ctx.accessToken, newAccessToken
        );
    }
}