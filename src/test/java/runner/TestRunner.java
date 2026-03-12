package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/auth",
        glue     = {"steps", "hooks"},
        plugin   = { "pretty", "html:target/reports/cucumber-report.html",
                "json:target/reports/cucumber-report.json"},
        tags     = "@regression"
)
public class TestRunner {}
