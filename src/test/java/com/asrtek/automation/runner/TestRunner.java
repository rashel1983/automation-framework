package com.asrtek.automation.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * # Validate all locators
mvn test -Dtest=LocatorValidationTest

# Validate only UI locators
mvn test -Dtest=LocatorValidationTest#validateUILocators

# Validate only Mobile locators
mvn test -Dtest=LocatorValidationTest#validateMobileLocators

 * JUnit 4 Cucumber test runner.
 *
 * Run all tests:   mvn test
 * Run specific tag: mvn test -Dcucumber.filter.tags="@smoke"
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/ui",
        glue = {
                "com.asrtek.automation.hooks",
                "com.asrtek.automation.steps"
        },
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-report.html",
                "json:target/cucumber-reports/cucumber-report.json"
        },
        monochrome = true,
       //tags = "@librarytest"
        //tags = "@addemployee"
       tags = "not @wip"
)
public class TestRunner {}
