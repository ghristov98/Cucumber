package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CompanyTestSteps {

    private final ScenarioContext ctx;

    public CompanyTestSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("I have a company test created with title {string}")
    public void createCompanyTest(String title) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "title": "%s",
                  "description": "Company test",
                  "time_limit_minutes": 30,
                  "max_attempts": 3,
                  "show_answers_after": false,
                  "folder": null
                }""", title))
                .when()
                .post("/tests/company/" + ctx.createdCompanyId + "/");

        assertEquals("Failed to create company test: " + r.statusCode()
                + ", body: " + r.getBody().asString(), 201, r.statusCode());
        ctx.companyTestSlug = r.jsonPath().getString("slug");
        System.out.println("Created company test slug: " + ctx.companyTestSlug);
    }

    // ── GET /tests/company/{id}/ ────────────────────────────────────────────

    @When("I request the list of company tests")
    public void getCompanyTests() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/");
    }

    @When("a student member requests the list of company tests")
    public void studentGetsCompanyTests() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/");
    }

    @When("a non-member requests the list of company tests")
    public void nonMemberGetsCompanyTests() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/");
    }

    // ── POST /tests/company/{id}/ ───────────────────────────────────────────

    @When("I create a company test with title {string}")
    public void createCompanyTestRequest(String title) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("""
                {
                  "title": "%s",
                  "description": "Test description",
                  "time_limit_minutes": 30,
                  "max_attempts": 3,
                  "show_answers_after": false,
                  "folder": null
                }""", title))
                .when()
                .post("/tests/company/" + ctx.createdCompanyId + "/");

        if (ctx.response.statusCode() == 201) {
            ctx.companyTestSlug = ctx.response.jsonPath().getString("slug");
        }
    }

    @When("an instructor creates a company test with title {string}")
    public void instructorCreatesCompanyTest(String title) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.instructorToken)
                .body(String.format("""
                {
                  "title": "%s",
                  "description": "Instructor test",
                  "time_limit_minutes": 30,
                  "max_attempts": 3,
                  "show_answers_after": false,
                  "folder": null
                }""", title))
                .when()
                .post("/tests/company/" + ctx.createdCompanyId + "/");

        if (ctx.response.statusCode() == 201) {
            ctx.companyTestSlug = ctx.response.jsonPath().getString("slug");
        }
    }

    @When("a student tries to create a company test with title {string}")
    public void studentCreatesCompanyTest(String title) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .body(String.format("""
                {
                  "title": "%s",
                  "description": "Student test",
                  "folder": null
                }""", title))
                .when()
                .post("/tests/company/" + ctx.createdCompanyId + "/");
    }

    // ── GET /tests/company/{id}/{slug}/ ─────────────────────────────────────

    @When("I request the company test details")
    public void getCompanyTestDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    @When("a student member requests the company test details")
    public void studentGetsCompanyTestDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    @When("a non-member requests the company test details")
    public void nonMemberGetsCompanyTestDetails() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    // ── PATCH /tests/company/{id}/{slug}/ ───────────────────────────────────

    @When("I update the company test title to {string}")
    public void updateCompanyTestTitle(String title) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"title\": \"%s\"}", title))
                .when()
                .patch("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    @When("an instructor updates the company test title to {string}")
    public void instructorUpdatesCompanyTest(String title) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.instructorToken)
                .body(String.format("{\"title\": \"%s\"}", title))
                .when()
                .patch("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    @When("a student tries to update the company test title to {string}")
    public void studentUpdatesCompanyTest(String title) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .body(String.format("{\"title\": \"%s\"}", title))
                .when()
                .patch("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    // ── DELETE /tests/company/{id}/{slug}/ ──────────────────────────────────

    @When("I delete the company test")
    public void deleteCompanyTest() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    @When("a student tries to delete the company test")
    public void studentDeletesCompanyTest() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .when()
                .delete("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the company test response should contain required fields")
    public void verifyCompanyTestFields() {
        ctx.response.then()
                .body("id",         notNullValue())
                .body("title",      notNullValue())
                .body("slug",       notNullValue())
                .body("visibility", equalTo("link_only"));
    }

    @Then("the response should indicate students cannot create tests")
    public void verifyStudentCannotCreateTest() {
        ctx.response.then()
                .statusCode(403)
                .body("detail", containsString("Students cannot create tests"));
    }

    @Then("the deleted company test should no longer be accessible")
    public void verifyCompanyTestDeleted() {
        io.restassured.response.Response r = given()
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/tests/company/" + ctx.createdCompanyId + "/" + ctx.companyTestSlug + "/");
        assertEquals("Expected 404 after delete but got: " + r.statusCode(), 404, r.statusCode());
    }
}