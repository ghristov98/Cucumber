package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MemberSteps {

    private final ScenarioContext ctx;

    public MemberSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("a second user is a member of the company with email {string} and password {string}")
    public void registerAndAddMember(String email, String password) {
        // Register second user
        given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Second",
                  "last_name": "Member",
                  "password": "%s",
                  "password_confirm": "%s"
                }""", email, password, password))
                .when()
                .post("/auth/register/");

        // Login second user
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

        assertEquals("Second user login failed: " + loginResp.statusCode(),
                200, loginResp.statusCode());
        ctx.secondAccessToken = loginResp.jsonPath().getString("access");

        // Get second user's ID via /auth/me/
        io.restassured.response.Response meResp = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/auth/me/");

        assertEquals("Failed to get second user profile: " + meResp.statusCode(),
                200, meResp.statusCode());
        ctx.secondUserId = meResp.jsonPath().getInt("id");

        // Admin creates invite for second user
        io.restassured.response.Response inviteResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "email": "%s",
                  "role": "student"
                }""", email))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");

        assertEquals("Failed to create invite: " + inviteResp.statusCode()
                + ", body: " + inviteResp.getBody().asString(), 201, inviteResp.statusCode());
        ctx.inviteToken = inviteResp.jsonPath().getString("token");

        // Second user accepts invite
        io.restassured.response.Response acceptResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .body("{}")
                .when()
                .post("/invites/" + ctx.inviteToken + "/accept/");

        assertEquals("Failed to accept invite: " + acceptResp.statusCode()
                + ", body: " + acceptResp.getBody().asString(), 200, acceptResp.statusCode());

        System.out.println("Second user ID: " + ctx.secondUserId + " added as member");
    }

    // ── GET /companies/{id}/members/ ────────────────────────────────────────

    @When("I request the list of company members")
    public void getCompanyMembers() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/members/");
    }

    @When("a non-member requests the list of company members")
    public void nonMemberGetsMembers() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/members/");
    }

    @When("an unauthenticated user requests the list of company members")
    public void unauthenticatedGetMembers() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/members/");
    }

    // ── PUT /companies/{id}/members/{user_id}/ ──────────────────────────────

    @When("the admin updates the second member role to {string}")
    public void updateMemberRole(String role) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"role\": \"%s\"}", role))
                .when()
                .put("/companies/" + ctx.createdCompanyId + "/members/" + ctx.secondUserId + "/");
    }

    @When("a non-admin tries to update the second member role to {string}")
    public void nonAdminUpdatesMemberRole(String role) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .body(String.format("{\"role\": \"%s\"}", role))
                .when()
                .put("/companies/" + ctx.createdCompanyId + "/members/" + ctx.secondUserId + "/");
    }

    @When("an unauthenticated user tries to update a member role")
    public void unauthenticatedUpdateMemberRole() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{\"role\": \"instructor\"}")
                .when()
                .put("/companies/" + ctx.createdCompanyId + "/members/" + ctx.secondUserId + "/");
    }

    @When("the admin tries to update their own role to {string}")
    public void adminUpdatesOwnRole(String role) {
        // Get admin's own ID first
        io.restassured.response.Response meResp = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/auth/me/");
        int adminId = meResp.jsonPath().getInt("id");

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"role\": \"%s\"}", role))
                .when()
                .put("/companies/" + ctx.createdCompanyId + "/members/" + adminId + "/");
    }

    // ── DELETE /companies/{id}/members/{user_id}/ ───────────────────────────

    @When("the admin removes the second member from the company")
    public void adminRemovesMember() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/members/" + ctx.secondUserId + "/");
    }

    @When("a non-admin tries to remove the second member")
    public void nonAdminRemovesMember() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/members/" + ctx.secondUserId + "/");
    }

    @When("an unauthenticated user tries to remove a member")
    public void unauthenticatedRemovesMember() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/members/" + ctx.secondUserId + "/");
    }

    @When("the admin tries to remove themselves as the last admin")
    public void adminRemovesThemself() {
        io.restassured.response.Response meResp = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/auth/me/");
        int adminId = meResp.jsonPath().getInt("id");

        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/members/" + adminId + "/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the member list should contain required fields")
    public void verifyMemberListFields() {
        ctx.response.then()
                .body("[0].user_id",   notNullValue())
                .body("[0].email",     notNullValue())
                .body("[0].role",      notNullValue())
                .body("[0].joined_at", notNullValue());
    }

    @Then("the member response should contain required fields")
    public void verifyMemberResponseFields() {
        ctx.response.then()
                .body("user_id",   notNullValue())
                .body("email",     notNullValue())
                .body("role",      notNullValue())
                .body("joined_at", notNullValue());
    }

    @Then("the member role should be {string}")
    public void verifyMemberRole(String expectedRole) {
        ctx.response.then().body("role", equalTo(expectedRole));
    }
}