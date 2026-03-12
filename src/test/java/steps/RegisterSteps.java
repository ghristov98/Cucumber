package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;

public class RegisterSteps {

    private final ScenarioContext ctx;

    public RegisterSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("a user already exists with email {string}")
    public void aUserAlreadyExistsWithEmail(String email) {
        given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Existing",
                  "last_name": "User",
                  "password": "SecurePass123!",
                  "password_confirm": "SecurePass123!"
                }""", email))
                .post("/auth/register/");
    }
}