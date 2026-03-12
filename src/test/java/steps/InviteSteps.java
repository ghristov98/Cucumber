package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class InviteSteps {

    private final ScenarioContext ctx;

    public InviteSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("an invite exists for email {string} with role {string}")
    public void createInvite(String email, String role) {
        ctx.inviteEmail = email;

        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "email": "%s",
                  "role": "%s"
                }""", email, role))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");

        assertEquals("Failed to create invite: " + r.statusCode()
                + ", body: " + r.getBody().asString(), 201, r.statusCode());
        ctx.inviteToken = r.jsonPath().getString("token");
        ctx.inviteId = r.jsonPath().getInt("id");
        System.out.println("Created invite token: " + ctx.inviteToken);
    }

    @Given("a user is registered with email {string} and password {string} for invite acceptance")
    public void registerUserForInvite(String email, String password) {
        given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Invite",
                  "last_name": "User",
                  "password": "%s",
                  "password_confirm": "%s"
                }""", email, password, password))
                .when()
                .post("/auth/register/");

        io.restassured.response.Response loginResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "password": "%s"
                }""", email, password))
                .when()
                .post("/auth/login/");

        assertEquals("Failed to login invite user: " + loginResp.statusCode(),
                200, loginResp.statusCode());
        ctx.secondAccessToken = loginResp.jsonPath().getString("access");
        assertNotNull("Invite user access token is null", ctx.secondAccessToken);
    }

    // ── GET /companies/{id}/invites/ ────────────────────────────────────────

    @When("the admin requests the list of company invites")
    public void getInviteList() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/invites/");
    }

    @When("a non-admin requests the list of company invites")
    public void nonAdminGetsInvites() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/invites/");
    }

    @When("an unauthenticated user requests the list of company invites")
    public void unauthenticatedGetInvites() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/invites/");
    }

    // ── POST /companies/{id}/invites/ ───────────────────────────────────────

    @When("the admin sends an invite to {string} with role {string}")
    public void adminSendsInvite(String email, String role) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "email": "%s",
                  "role": "%s"
                }""", email, role))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");

        if (ctx.response.statusCode() == 201) {
            ctx.inviteToken = ctx.response.jsonPath().getString("token");
            ctx.inviteId    = ctx.response.jsonPath().getInt("id");
            ctx.inviteEmail = email;
        }
    }

    @When("a non-admin tries to send an invite to {string}")
    public void nonAdminSendsInvite(String email) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .body(String.format("""
                {
                  "email": "%s",
                  "role": "student"
                }""", email))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");
    }

    @When("an unauthenticated user tries to send an invite")
    public void unauthenticatedSendsInvite() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{\"email\": \"anyone@example.com\", \"role\": \"student\"}")
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");
    }

    // ── POST /invites/{token}/accept/ ───────────────────────────────────────

    @When("the invited user accepts the invite")
    public void invitedUserAcceptsInvite() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .body("{}")
                .when()
                .post("/invites/" + ctx.inviteToken + "/accept/");
    }

    @When("the invited user tries to accept the invite again")
    public void invitedUserAcceptsAgain() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .body("{}")
                .when()
                .post("/invites/" + ctx.inviteToken + "/accept/");
    }

    @When("a different user tries to accept the invite")
    public void differentUserAcceptsInvite() {
        // Use admin token — email won't match the invite
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("{}")
                .when()
                .post("/invites/" + ctx.inviteToken + "/accept/");
    }

    @When("an unauthenticated user tries to accept the invite")
    public void unauthenticatedAcceptsInvite() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{}")
                .when()
                .post("/invites/" + ctx.inviteToken + "/accept/");
    }

    @When("a user tries to accept an invite with an invalid token")
    public void acceptWithInvalidToken() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body("{}")
                .when()
                .post("/invites/invalid-token-xyz/accept/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the invite list should contain required fields")
    public void verifyInviteListFields() {
        String body = ctx.response.getBody().asString();
        if (!body.equals("[]")) {
            ctx.response.then()
                    .body("[0].id",         notNullValue())
                    .body("[0].email",      notNullValue())
                    .body("[0].role",       notNullValue())
                    .body("[0].token",      notNullValue())
                    .body("[0].created_at", notNullValue())
                    .body("[0].accepted",   notNullValue());
        }
    }

    @Then("the invite response should contain required fields")
    public void verifyInviteResponseFields() {
        ctx.response.then()
                .body("id",         notNullValue())
                .body("email",      notNullValue())
                .body("role",       notNullValue())
                .body("token",      notNullValue())
                .body("created_at", notNullValue())
                .body("accepted",   equalTo(false));
    }

    @Then("the accept response should confirm the user joined")
    public void verifyAcceptResponse() {
        ctx.response.then()
                .body("status",     equalTo("joined"))
                .body("company_id", equalTo(ctx.createdCompanyId));
    }

    @Then("the response should indicate email mismatch")
    public void verifyEmailMismatch() {
        ctx.response.then()
                .body("detail", containsString("different email"));
    }

    @Then("the response should indicate invite already accepted")
    public void verifyAlreadyAccepted() {
        ctx.response.then()
                .body("detail", containsString("already been accepted"));
    }
}