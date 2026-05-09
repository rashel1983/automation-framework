package com.asrtek.automation.api.steps;

import com.asrtek.automation.api.context.ApiContext;
import com.asrtek.automation.api.hooks.ApiHooks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.Assert;

/**
 * Generic API step definitions reusable across all feature files.
 *
 * Covers:
 *   - Health / connectivity check
 *   - Response status code assertions
 *   - Response body field assertions
 *   - Saving values from response for later steps
 */
public class CommonApiSteps {

    // ── Connectivity ──────────────────────────────────────────────────────────

    @Given("the API is running")
    public void theApiIsRunning() {
        Response response = ApiHooks.getClient().getPublic("/actuator/health");
        Assert.assertEquals(
                "[API Health] Expected 200 but got " + response.statusCode(),
                200, response.statusCode());
    }

    // ── Health endpoint ───────────────────────────────────────────────────────

    @When("I check the application health")
    public void iCheckTheApplicationHealth() {
        ApiHooks.getClient().getPublic("/actuator/health");
    }

    // ── Status code assertions ────────────────────────────────────────────────

    @Then("the API response status should be {int}")
    public void theApiResponseStatusShouldBe(int expectedStatus) {
        int actualStatus = ApiContext.getResponse().statusCode();
        Assert.assertEquals(
                "Expected HTTP " + expectedStatus + " but got " + actualStatus,
                expectedStatus, actualStatus);
    }

    // ── Body field assertions ─────────────────────────────────────────────────

    @Then("the API response should contain field {string}")
    public void theResponseShouldContainField(String field) {
        Object value = ApiContext.getResponse().jsonPath().get(field);
        Assert.assertNotNull(
                "Expected field '" + field + "' in response but it was missing or null.\n"
                        + "Response: " + ApiContext.getResponse().asPrettyString(),
                value);
    }

    @Then("the API response should contain field {string} with value {string}")
    public void theResponseShouldContainFieldWithValue(String field, String expectedValue) {
        String actual = ApiContext.getResponse().jsonPath().getString(field);
        Assert.assertEquals(
                "Field '" + field + "' mismatch.\nResponse: "
                        + ApiContext.getResponse().asPrettyString(),
                expectedValue, actual);
    }

    @Then("the API response list should not be empty")
    public void theResponseListShouldNotBeEmpty() {
        int size = ApiContext.getResponse().jsonPath().getList("$").size();
        Assert.assertTrue(
                "Expected at least one item in the response list but got 0.\n"
                        + "Response: " + ApiContext.getResponse().asPrettyString(),
                size > 0);
    }

    @Then("the API response list should have at least {int} items")
    public void theResponseListShouldHaveAtLeastItems(int minCount) {
        int size = ApiContext.getResponse().jsonPath().getList("$").size();
        Assert.assertTrue(
                "Expected at least " + minCount + " items but got " + size,
                size >= minCount);
    }

    // ── Value extraction ──────────────────────────────────────────────────────

    @Then("I save the response field {string} as {string}")
    public void iSaveResponseFieldAs(String field, String alias) {
        String value = ApiContext.getResponse().jsonPath().getString(field);
        Assert.assertNotNull("Cannot save null field '" + field + "'", value);
        ApiContext.save(alias, value);
        System.out.println("[CommonApiSteps] Saved " + alias + " = " + value);
    }
}
