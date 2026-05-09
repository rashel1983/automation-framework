package com.asrtek.automation.mobile.hooks;

import com.asrtek.automation.core.LocatorLoader;
import com.asrtek.automation.core.PageContext;
import com.asrtek.automation.mobile.actions.MobileActionHelper;
import com.asrtek.automation.mobile.config.MobileConfig;
import com.asrtek.automation.mobile.driver.MobileDriverManager;
import com.asrtek.automation.utils.ExtentReportManager;
import com.asrtek.automation.utils.JsonUtils;
import com.asrtek.automation.models.UserModel;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Cucumber lifecycle hooks for mobile tests only.
 * Mirrors the existing Hooks.java exactly — same order, same responsibilities,
 * swaps WebDriver for AppiumDriver.
 */
public class MobileHooks {

    private static final ThreadLocal<MobileActionHelper> actionHelper = new ThreadLocal<>();

    /** Load all JSON locators into JVM memory once per test run (idempotent). */
    @Before(order = 0)
    public void loadLocators(Scenario scenario) {
        LocatorLoader.loadAll();
        scenario.log("[MobileHooks] Locators loaded. Platform: " + MobileConfig.getPlatform()
                + " | Type: " + MobileConfig.getMobileType());
    }

    /** Register a new ExtentTest node for this scenario. */
    @Before(order = 1)
    public void initReport(Scenario scenario) {
        ExtentReportManager.createTest("[Mobile] " + scenario.getName());
    }

    /** Create (or reuse) AppiumDriver and navigate to the app/base URL. */
    @Before(order = 2)
    public void startDriver(Scenario scenario) {
        String userName = resolveUser(scenario);
        AppiumDriver driver = MobileDriverManager.getOrCreateSession(userName);
        actionHelper.set(new MobileActionHelper(driver));

        if ("web".equalsIgnoreCase(MobileConfig.getMobileType())) {
            driver.get(MobileConfig.getBaseUrl());
        }

        PageContext.setCurrentPage("MobileLoginPage");
        System.out.println("[MobileHooks] Session ready for: " + userName);
    }

    /** Screenshot on failure + log result. */
    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                AppiumDriver driver = MobileDriverManager.getDriver();
                byte[] screenshot = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
                ExtentReportManager.logFail(scenario.getName() + " failed");
            } catch (Exception e) {
                System.out.println("[MobileHooks] Screenshot failed: " + e.getMessage());
            }
        } else {
            ExtentReportManager.logPass(scenario.getName() + " passed");
        }
        PageContext.reset();
        actionHelper.remove();
    }

    @AfterAll
    public static void closeSessions() {
        MobileDriverManager.quitAll();
        ExtentReportManager.flush();
    }

    /** Expose ActionHelper to step definitions on the same thread. */
    public static MobileActionHelper getActionHelper() {
        MobileActionHelper helper = actionHelper.get();
        if (helper == null) {
            throw new IllegalStateException(
                    "[MobileHooks] MobileActionHelper not initialised. "
                    + "Ensure the @Before hook ran before steps execute.");
        }
        return helper;
    }

    private String resolveUser(Scenario scenario) {
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@user:")) return tag.substring(6);
        }
        return "MobileUser";
    }
}
