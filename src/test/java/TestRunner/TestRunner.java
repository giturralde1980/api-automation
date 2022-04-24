package TestRunner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber"},
        features = {"src/test/java/Features"},
        glue={"src/test/java/Steps"},
        monochrome = true,
        dryRun = false
)
public class TestRunner {





}


