package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CompanySteps {

    private final ScenarioContext ctx;

    public CompanySteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("I have a company created with name {string}")
    public void createCompany(String name) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\"}", name))
                .when()
                .post("/companies/");

        assertEquals("Failed to create company, status: " + r.statusCode()
                + ", body: " + r.getBody().asString(), 201, r.statusCode());
        ctx.createdCompanyId = r.jsonPath().getInt("id");
        System.out.println("Created company ID: " + ctx.createdCompanyId);
    }

    @Given("a second user is registered with email {string} and password {string}")
    public void registerSecondUser(String email, String password) {
        // Register
        given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Second",
                  "last_name": "User",
                  "password": "%s",
                  "password_confirm": "%s"
                }""", email, password, password))
                .when()
                .post("/auth/register/");

        // Login and save token separately
        io.restassured.response.Response loginResponse = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "password": "%s"
                }""", email, password))
                .when()
                .post("/auth/login/");

        assertEquals("Failed to login second user, status: " + loginResponse.statusCode(),
                200, loginResponse.statusCode());
        ctx.secondAccessToken = loginResponse.jsonPath().getString("access");
        assertNotNull("Second user access token is null", ctx.secondAccessToken);
    }

    // ── GET /companies/ ─────────────────────────────────────────────────────

    @When("I request the list of my companies")
    public void getCompanyList() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/");
    }

    // ── POST /companies/ ────────────────────────────────────────────────────

    @When("I send a create company request with name {string}")
    public void sendCreateCompanyRequest(String name) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\"}", name))
                .when()
                .post("/companies/");

        if (ctx.response.statusCode() == 201) {
            ctx.createdCompanyId = ctx.response.jsonPath().getInt("id");
        }
    }

    @When("an unauthenticated user sends a create company request")
    public void unauthenticatedCreateCompany() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{\"name\": \"Unauthorized Corp\"}")
                .when()
                .post("/companies/");
    }

    // ── GET /companies/{id}/ ────────────────────────────────────────────────

    @When("I request the company details")
    public void getCompanyDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/");
    }

    @When("a non-member requests the company details")
    public void nonMemberGetsCompanyDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/");
    }

    @When("an unauthenticated user requests the company details")
    public void unauthenticatedGetCompanyDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/");
    }

    // ── PATCH /companies/{id}/ ──────────────────────────────────────────────

    @When("I update the company name to {string}")
    public void updateCompanyName(String newName) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\"}", newName))
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/");
    }

    @When("a non-admin tries to update the company name to {string}")
    public void nonAdminUpdatesCompany(String newName) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .body(String.format("{\"name\": \"%s\"}", newName))
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/");
    }

    @When("an unauthenticated user tries to update the company")
    public void unauthenticatedUpdateCompany() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{\"name\": \"Hacked Name\"}")
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/");
    }

    // ── DELETE /companies/{id}/ ─────────────────────────────────────────────

    @When("I delete the company")
    public void deleteCompany() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/");
    }

    @When("a non-admin tries to delete the company")
    public void nonAdminDeletesCompany() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/");
    }

    @When("an unauthenticated user tries to delete the company")
    public void unauthenticatedDeleteCompany() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the company response should contain required fields")
    public void verifyCompanyFields() {
        ctx.response.then()
                .body("id",           notNullValue())
                .body("name",         notNullValue())
                .body("member_count", notNullValue())
                .body("my_role",      notNullValue())
                .body("created_at",   notNullValue());
    }

    @Then("the company creator should have admin role")
    public void verifyCreatorIsAdmin() {
        ctx.response.then()
                .body("my_role", equalTo("admin"));
    }

    @Then("the company list should contain required fields")
    public void verifyCompanyListFields() {
        // Only verify structure if list is not empty
        String body = ctx.response.getBody().asString();
        if (!body.equals("[]")) {
            ctx.response.then()
                    .body("[0].id",           notNullValue())
                    .body("[0].name",         notNullValue())
                    .body("[0].member_count", notNullValue())
                    .body("[0].my_role",      notNullValue())
                    .body("[0].created_at",   notNullValue());
        }
    }

    @Then("the deleted company should no longer be accessible")
    public void verifyCompanyDeleted() {
        io.restassured.response.Response r = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/");

        assertEquals("Expected 404 after delete but got: " + r.statusCode()
                + ", body: " + r.getBody().asString(), 404, r.statusCode());
    }
}