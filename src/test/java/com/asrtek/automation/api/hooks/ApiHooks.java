package com.asrtek.automation.api.hooks;

import com.asrtek.automation.api.client.ApiClient;
import com.asrtek.automation.api.context.ApiContext;
import com.asrtek.automation.config.ConfigReader;
import com.asrtek.automation.utils.ExtentReportManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Cucumber lifecycle hooks for API tests only.
 * Wired in by ApiTestRunner via the com.asrtek.automation.api.hooks glue package.
 * Does NOT start a browser — purely HTTP.
 */
public class ApiHooks {

    private static volatile ApiClient sharedClient;
    private static final Object lock = new Object();

    /** Initialise the shared ApiClient once per test run. */
    @Before(order = 0)
    public void initApiClient(Scenario scenario) {
        if (sharedClient == null) {
            synchronized (lock) {
                if (sharedClient == null) {
                    String baseUrl = ConfigReader.getInstance().getApiBaseUrl();
                    sharedClient = new ApiClient(baseUrl);
                    System.out.println("[ApiHooks] ApiClient initialised → " + baseUrl);
                }
            }
        }
        scenario.log("[ApiHooks] Starting API scenario: " + scenario.getName());
    }

    /** Register a new ExtentTest node for this scenario. */
    @Before(order = 1)
    public void initReport(Scenario scenario) {
        ExtentReportManager.createTest(scenario.getName());
    }

    /** Clean up thread-local state after each scenario. */
    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            String responseBody = ApiContext.getResponse() != null
                    ? ApiContext.getResponse().asPrettyString()
                    : "No response captured";
            scenario.log("[ApiHooks] FAILED — Last response:\n" + responseBody);
            ExtentReportManager.logFail("Scenario failed. Last response:\n" + responseBody);
        } else {
            ExtentReportManager.logPass(scenario.getName() + " passed");
        }
        ApiContext.reset();
    }

    /** Expose the shared client to step definitions. */
    public static ApiClient getClient() {
        if (sharedClient == null) {
            throw new IllegalStateException("[ApiHooks] ApiClient not initialised yet.");
        }
        return sharedClient;
    }
}
