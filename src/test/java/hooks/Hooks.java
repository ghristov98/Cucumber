package hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import steps.ScenarioContext;

public class Hooks {

    private final ScenarioContext ctx;

    public Hooks(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Before
    public void setUp(Scenario scenario) {
        RestAssured.useRelaxedHTTPSValidation();
        ctx.sessionFilter = new SessionFilter(); // ← fresh session each scenario
        System.out.println("Starting: " + scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            System.out.println("FAILED: " + scenario.getName());
        }
    }
}