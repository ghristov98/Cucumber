package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FolderSteps {

    private final ScenarioContext ctx;

    public FolderSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // ── Pre-conditions ──────────────────────────────────────────────────────

    @Given("I have a folder created with name {string}")
    public void createFolder(String name) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\", \"parent\": null}", name))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/folders/");

        assertEquals("Failed to create folder: " + r.statusCode()
                + ", body: " + r.getBody().asString(), 201, r.statusCode());
        ctx.createdFolderId = r.jsonPath().getInt("id");
        System.out.println("Created folder ID: " + ctx.createdFolderId);
    }

    @Given("I have a second folder created with name {string}")
    public void createSecondFolder(String name) {
        io.restassured.response.Response r = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\", \"parent\": null}", name))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/folders/");

        assertEquals("Failed to create second folder: " + r.statusCode()
                + ", body: " + r.getBody().asString(), 201, r.statusCode());
        ctx.secondFolderId = r.jsonPath().getInt("id");
        System.out.println("Created second folder ID: " + ctx.secondFolderId);
    }

    @Given("an instructor is a member of the company with email {string} and password {string}")
    public void addInstructorMember(String email, String password) {
        // Register
        given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Instructor",
                  "last_name": "User",
                  "password": "%s",
                  "password_confirm": "%s"
                }""", email, password, password))
                .when()
                .post("/auth/register/");

        // Login
        io.restassured.response.Response loginResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password))
                .when()
                .post("/auth/login/");

        assertEquals("Instructor login failed: " + loginResp.statusCode(),
                200, loginResp.statusCode());
        ctx.instructorToken = loginResp.jsonPath().getString("access");

        // Create invite with instructor role
        io.restassured.response.Response inviteResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"email\": \"%s\", \"role\": \"instructor\"}", email))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");

        assertEquals("Failed to create instructor invite: " + inviteResp.statusCode()
                + ", body: " + inviteResp.getBody().asString(), 201, inviteResp.statusCode());
        String token = inviteResp.jsonPath().getString("token");

        // Accept invite
        io.restassured.response.Response acceptResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.instructorToken)
                .body("{}")
                .when()
                .post("/invites/" + token + "/accept/");

        assertEquals("Instructor failed to accept invite: " + acceptResp.statusCode()
                + ", body: " + acceptResp.getBody().asString(), 200, acceptResp.statusCode());
        System.out.println("Instructor added to company");
    }

    @Given("a student is a member of the company with email {string} and password {string}")
    public void addStudentMember(String email, String password) {
        // Register
        given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("""
                {
                  "email": "%s",
                  "first_name": "Student",
                  "last_name": "User",
                  "password": "%s",
                  "password_confirm": "%s"
                }""", email, password, password))
                .when()
                .post("/auth/register/");

        // Login
        io.restassured.response.Response loginResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password))
                .when()
                .post("/auth/login/");

        assertEquals("Student login failed: " + loginResp.statusCode(),
                200, loginResp.statusCode());
        ctx.studentToken = loginResp.jsonPath().getString("access");

        // Create invite with student role
        io.restassured.response.Response inviteResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"email\": \"%s\", \"role\": \"student\"}", email))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/invites/");

        assertEquals("Failed to create student invite: " + inviteResp.statusCode()
                + ", body: " + inviteResp.getBody().asString(), 201, inviteResp.statusCode());
        String token = inviteResp.jsonPath().getString("token");

        // Accept invite
        io.restassured.response.Response acceptResp = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .body("{}")
                .when()
                .post("/invites/" + token + "/accept/");

        assertEquals("Student failed to accept invite: " + acceptResp.statusCode()
                + ", body: " + acceptResp.getBody().asString(), 200, acceptResp.statusCode());
        System.out.println("Student added to company");
    }

    // ── GET /companies/{id}/folders/ ────────────────────────────────────────

    @When("I request the list of company folders")
    public void getFolderList() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/folders/");
    }

    @When("a student member requests the list of company folders")
    public void studentGetsFolderList() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/folders/");
    }

    @When("a non-member requests the list of company folders")
    public void nonMemberGetsFolderList() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.secondAccessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/folders/");
    }

    // ── POST /companies/{id}/folders/ ───────────────────────────────────────

    @When("I send a create folder request with name {string}")
    public void createFolderRequest(String name) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\", \"parent\": null}", name))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/folders/");

        if (ctx.response.statusCode() == 201) {
            ctx.createdFolderId = ctx.response.jsonPath().getInt("id");
        }
    }

    @When("an instructor creates a folder with name {string}")
    public void instructorCreatesFolder(String name) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.instructorToken)
                .body(String.format("{\"name\": \"%s\", \"parent\": null}", name))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/folders/");

        if (ctx.response.statusCode() == 201) {
            ctx.createdFolderId = ctx.response.jsonPath().getInt("id");
        }
    }

    @When("a student tries to create a folder with name {string}")
    public void studentCreatesFolder(String name) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .body(String.format("{\"name\": \"%s\", \"parent\": null}", name))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/folders/");
    }

    @When("I create a child folder under the first folder")
    public void createChildFolder() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"Child Folder\", \"parent\": %d}",
                        ctx.createdFolderId))
                .when()
                .post("/companies/" + ctx.createdCompanyId + "/folders/");

        if (ctx.response.statusCode() == 201) {
            ctx.secondFolderId = ctx.response.jsonPath().getInt("id");
        }
    }

    // ── PATCH /companies/{id}/folders/{folder_id}/ ──────────────────────────

    @When("I update the folder name to {string}")
    public void updateFolderName(String name) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"name\": \"%s\"}", name))
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    @When("I try to set the folder's parent to itself")
    public void setFolderParentToItself() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"parent\": %d}", ctx.createdFolderId))
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    @When("I try to create a circular folder hierarchy")
    public void createCircularHierarchy() {
        // createdFolderId is parent of secondFolderId
        // Now try to make createdFolderId a child of secondFolderId — circular
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .body(String.format("{\"parent\": %d}", ctx.secondFolderId))
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    @When("a student tries to update the folder name to {string}")
    public void studentUpdatesFolder(String name) {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .body(String.format("{\"name\": \"%s\"}", name))
                .when()
                .patch("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    // ── DELETE /companies/{id}/folders/{folder_id}/ ─────────────────────────

    @When("I delete the folder")
    public void deleteFolder() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    @When("a student tries to delete the folder")
    public void studentDeletesFolder() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ctx.studentToken)
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    @When("an unauthenticated user tries to delete the folder")
    public void unauthenticatedDeletesFolder() {
        ctx.response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .delete("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
    }

    // ── Assertions ──────────────────────────────────────────────────────────

    @Then("the folder response should contain required fields")
    public void verifyFolderFields() {
        ctx.response.then()
                .body("id",         notNullValue())
                .body("name",       notNullValue());
    }

    @Then("the folder list should contain required fields")
    public void verifyFolderListFields() {
        String body = ctx.response.getBody().asString();
        if (!body.equals("[]")) {
            ctx.response.then()
                    .body("[0].id",         notNullValue())
                    .body("[0].name",       notNullValue());
        }
    }

    @Then("the response should indicate circular hierarchy is not allowed")
    public void verifyCircularHierarchyError() {
        String body = ctx.response.getBody().asString();
        assertTrue(
                "Expected circular hierarchy error but got: " + body,
                body.contains("Circular") || body.contains("circular")
        );
    }

    @Then("the response should indicate folder cannot be its own parent")
    public void verifySelfReferenceError() {
        String body = ctx.response.getBody().asString();
        assertTrue(
                "Expected self-reference error but got: " + body,
                body.contains("own parent") || body.contains("Circular") || body.contains("circular")
        );
    }

    @Then("the deleted folder should no longer be accessible")
    public void verifyFolderDeleted() {
        io.restassured.response.Response r = given()
                .header("Authorization", "Bearer " + ctx.accessToken)
                .when()
                .get("/companies/" + ctx.createdCompanyId + "/folders/" + ctx.createdFolderId + "/");
        assertEquals("Expected 404 after delete but got: " + r.statusCode(), 404, r.statusCode());
    }
}