package com.asrtek.automation.hooks;

import com.asrtek.automation.config.ConfigReader;
import com.asrtek.automation.core.DriverManager;
import com.asrtek.automation.core.LocatorLoader;
import com.asrtek.automation.library.ScenarioLibrary;
import com.asrtek.automation.core.PageContext;
import com.asrtek.automation.utils.ExtentReportManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Cucumber lifecycle hooks.
 *
 * @Before order=0  — print config, load all JSON locators into JVM memory once.
 * @Before order=1  — create an ExtentTest entry for the scenario.
 * @After           — screenshot on failure (if enabled in config), log result, reset page.
 * @AfterAll        — flush HTML report, quit all browser sessions.
 */
public class Hooks {

    @Before(order = 0)
    public void loadLocators() {
        ConfigReader.getInstance().printConfig();
        LocatorLoader.loadAll();
        ScenarioLibrary.load();
    }

    @Before(order = 1)
    public void startTest(Scenario scenario) {
        ExtentReportManager.createTest(scenario.getName());
        ExtentReportManager.logInfo("Scenario started: " + scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            ExtentReportManager.logFail("FAILED: " + scenario.getName());
            if (ConfigReader.getInstance().isScreenshotOnFailure()) {
                captureScreenshot(scenario);
            }
        } else {
            ExtentReportManager.logPass("PASSED: " + scenario.getName());
        }
        PageContext.reset();
    }

    @AfterAll
    public static void afterAll() {
        DriverManager.quitAll();
        ExtentReportManager.flush();
        System.out.println("[Hooks] Suite complete — report written to target/extent-reports/");
    }

    private void captureScreenshot(Scenario scenario) {
        try {
            WebDriver driver = DriverManager.getDriver();
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(png, "image/png", "Failure screenshot");
            ExtentReportManager.logInfo("Screenshot captured.");
        } catch (Exception e) {
            System.out.println("[Hooks] Screenshot failed: " + e.getMessage());
        }
    }
}
