package com.asrtek.automation.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * API-only test runner.
 *
 * Runs ONLY the feature files in features/api/ using the API step definitions.
 * Does NOT start a browser.
 *
 * Run all API tests:
 *   mvn test -Dtest=ApiTestRunner
 *
 * Run only smoke API tests:
 *   mvn test -Dtest=ApiTestRunner -Dcucumber.filter.tags="@smoke"
 *
 * Run only auth API tests:
 *   mvn test -Dtest=ApiTestRunner -Dcucumber.filter.tags="@auth"
 *
 * Run against a specific environment:
 *   mvn test -Dtest=ApiTestRunner -Denv=qa
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/api",
        glue     = {
                "com.asrtek.automation.api.hooks",
                "com.asrtek.automation.api.steps"
        },
        tags     = "not @wip",
        plugin   = {
                "pretty",
                "html:target/cucumber-reports/api/cucumber-report.html",
                "json:target/cucumber-reports/api/cucumber-report.json"
        },
        monochrome = true
)
public class ApiTestRunner {}
