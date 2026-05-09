package com.asrtek.automation.steps;

import com.asrtek.automation.config.ConfigReader;
import com.asrtek.automation.core.ActionHelper;
import com.asrtek.automation.core.DriverManager;
import com.asrtek.automation.core.DriverUtil;
import com.asrtek.automation.core.PageContext;
import com.asrtek.automation.models.UserModel;
import com.asrtek.automation.utils.ExtentReportManager;
import com.asrtek.automation.utils.JsonUtils;
import com.asrtek.automation.utils.WaitUtils;
import com.asrtek.automation.library.ScenarioLibrary;
import com.asrtek.automation.library.StepInvoker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber step definitions — no instance state.
 *
 * Driver access:   DriverUtil.getDriver()
 * Locator access:  JsonUtils.jsonReaderWithNavigate(label) → JsonUtils.getBy(encoded)
 * Page tracking:   PageContext.getCurrentPageTitle() / getNavigationPath()
 */
public class CommonSteps {

    private static final Logger logger = LoggerFactory.getLogger(CommonSteps.class);

    // -------------------------------------------------------------------------
    // Given
    // -------------------------------------------------------------------------

    @Given("^I am logged in as an? \"([^\"]*)\" User$")
    public void iAmLoggedInAs(String userType) {
        UserModel user = JsonUtils.findUserByType(userType);

        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: Given I am logged in as '{}' User", userType);
        logger.info("  userName: {}", user.getUserName());

        boolean isAdmin = "Admin".equalsIgnoreCase(userType)
                || "Super Admin".equalsIgnoreCase(userType);
        PageContext.setCurrentUserRole(isAdmin ? "admin" : "app-user");

        DriverManager.getOrCreateSession(user.getUserName());

        String currentUrl = DriverUtil.getDriver().getCurrentUrl();
        if (currentUrl != null && !currentUrl.contains("/login") && !currentUrl.equals("data:,")) {
            logger.info("  ✔ Session reused — skipping login. URL: {}", currentUrl);
            // Set page context so subsequent steps find the right locator map
            PageContext.setCurrentPage(isAdmin ? "DashboardPage" : "AppUserHomePage");
            ExtentReportManager.logInfo("Session reused for: " + user.getUserName());
            return;
        }

        DriverUtil.getDriver().get(ConfigReader.getInstance().getBaseUrl() + "/login");
        WaitUtils.waitForPageLoad(DriverUtil.getDriver());
        WaitUtils.waitForAngular(DriverUtil.getDriver());

        ActionHelper actions = new ActionHelper(DriverUtil.getDriver());
        actions.type("Username", user.getUserName());
        actions.type("Password", user.getPass());
        actions.click("Sign In");

        WaitUtils.waitForAngular(DriverUtil.getDriver());

        // Sign In navigateTo always sets DashboardPage; override for app-user role
        if (!isAdmin) {
            PageContext.setCurrentPage("AppUserHomePage");
        }

        logger.info("  ✔ Logged in — page context: '{}'", PageContext.getCurrentPageTitle());
        ExtentReportManager.logPass("Logged in as " + userType);
    }

    @Given("^I navigate to the login page$")
    public void iNavigateToLoginPage() {
        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: Given I navigate to the login page");

        if (PageContext.getCurrentPageTitle() == null) {
            DriverManager.getOrCreateSession("anonymous");
        }
        DriverUtil.getDriver().get(ConfigReader.getInstance().getBaseUrl() + "/login");
        WaitUtils.waitForPageLoad(DriverUtil.getDriver());
        WaitUtils.waitForAngular(DriverUtil.getDriver());
        logger.info("  ✔ Navigation path: {}", PageContext.getNavigationPath());
    }

    // -------------------------------------------------------------------------
    // When
    // -------------------------------------------------------------------------

    @When("^I enter \"([^\"]*)\" into \"([^\"]*)\" field$")
    public void iEnterIntoField(String value, String fieldLabel) {
        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: When I enter '{}' into '{}' field", value, fieldLabel);
        logger.info("  Page context: '{}'", PageContext.getCurrentPageTitle());

        new ActionHelper(DriverUtil.getDriver()).type(fieldLabel, value);

        logger.info("  ✔ Typed '{}' into '{}'", value, fieldLabel);
        ExtentReportManager.logInfo("Typed '" + value + "' into '" + fieldLabel + "'");
    }

    @When("^I click on \"([^\"]*)\" option in side menu$")
    public void iClickOnSideMenuOption(String option) {
        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: When I click on '{}' option in side menu", option);

        String pageBefore = PageContext.getCurrentPageTitle();
        new ActionHelper(DriverUtil.getDriver()).clickOnPage("SidebarPage", option);
        String pageAfter = PageContext.getCurrentPageTitle();
        if (!String.valueOf(pageBefore).equals(String.valueOf(pageAfter))) {
            logger.info("  \u27f6 navigateTo fired: '{}' \u2192 '{}'", pageBefore, pageAfter);
        }
        logger.info("  Navigation path: {}", PageContext.getNavigationPath());
        ExtentReportManager.logInfo("Clicked sidebar: " + option);
    }

    @When("^I click on \"([^\"]*)\" from Dashboard$")
    public void iClickOnFromDashboard(String option) {
        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: When I click on '{}' from Dashboard", option);

        logger.info("  Page context: '{}'", PageContext.getCurrentPageTitle());

        new ActionHelper(DriverUtil.getDriver()).click(option);

        logger.info("  Navigation path: {}", PageContext.getNavigationPath());
        ExtentReportManager.logInfo("Clicked dashboard card: " + option);
    }

    @When("^I click on \"([^\"]*)\" button$")
    public void iClickOnButton(String label) {
        String pageBefore = PageContext.getCurrentPageTitle();

        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: When I click on '{}' button", label);
        logger.info("  Page context BEFORE click: '{}'", pageBefore);

        if (pageBefore == null) {
            logger.warn("  \u26a0 Page context is null — add step: Then I should be on the \"<page>\" page");
        }

        String locator = JsonUtils.jsonReaderWithNavigate(label);
        String pageAfter = PageContext.getCurrentPageTitle();

        if (!String.valueOf(pageBefore).equals(String.valueOf(pageAfter))) {
            logger.info("  \u27f6 navigateTo fired: page context '{}' \u2192 '{}'", pageBefore, pageAfter);
            logger.info("  \u2714 Next step will look up locators in '{}' map in JVM memory", pageAfter);
        }

        try {
            WebElement element = WaitUtils.waitForClickable(DriverUtil.getDriver(), JsonUtils.getBy(locator));
            element.click();
            WaitUtils.waitForPageLoad(DriverUtil.getDriver());
            WaitUtils.waitForAngular(DriverUtil.getDriver());
            logger.info("  \u2714 Clicked on '{}' — locator: '{}'", label, locator);
            logger.info("  Navigation path: {}", PageContext.getNavigationPath());
        } catch (Exception e) {
            logger.error("  \u2718 Unable to click '{}' — locator:'{}' — {}", label, locator, e.getMessage());
            throw e;
        }

        ExtentReportManager.logInfo("Clicked button: " + label);
    }

    @When("^I click on \"([^\"]*)\" link$")
    public void iClickOnLink(String label) {
        logger.info("STEP: When I click on '{}' link", label);
        DriverUtil.getDriver().findElement(JsonUtils.getBy(label)).click();
    }

    @When("^I click on \"([^\"]*)\" tab$")
    public void iClickOnTab(String label) {
        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: When I click on '{}' tab", label);
        logger.info("  Page context: '{}'", PageContext.getCurrentPageTitle());

        String locator = JsonUtils.jsonReaderWithNavigate(label);
        WaitUtils.waitForClickable(DriverUtil.getDriver(), JsonUtils.getBy(locator)).click();
        WaitUtils.waitForPageLoad(DriverUtil.getDriver());
        WaitUtils.waitForAngular(DriverUtil.getDriver());

        logger.info("  ✔ Clicked tab '{}' — page context: '{}'", label, PageContext.getCurrentPageTitle());
        ExtentReportManager.logInfo("Clicked tab: " + label);
    }

    @When("^I select \"([^\"]*)\" from \"([^\"]*)\" dropdown$")
    public void iSelectFromDropdown(String value, String dropdownLabel) {
        logger.info("──────────────────────────────────────────────");
        logger.info("STEP: When I select '{}' from '{}' dropdown", value, dropdownLabel);
        logger.info("  Page context: '{}'", PageContext.getCurrentPageTitle());

        String locator = JsonUtils.jsonReaderWithNavigate(dropdownLabel);
        try {
            new org.openqa.selenium.support.ui.Select(
                    WaitUtils.waitForVisible(DriverUtil.getDriver(), JsonUtils.getBy(locator)))
                    .selectByVisibleText(value);
            logger.info("  \u2714 Selected '{}' from '{}'", value, dropdownLabel);
        } catch (Exception e) {
            logger.error("  \u2718 Unable to select '{}' from '{}' — {}", value, dropdownLabel, e.getMessage());
            throw e;
        }

        ExtentReportManager.logInfo("Selected '" + value + "' from '" + dropdownLabel + "'");
    }

    // -------------------------------------------------------------------------
    // Then
    // -------------------------------------------------------------------------

    @Then("^I should see \"([^\"]*)\"$")
    public void iShouldSee(String labelKey) {
        logger.info("STEP: Then I should see '{}'", labelKey);

        String locator = JsonUtils.jsonReaderWithNavigate(labelKey);
        boolean visible;
        try {
            visible = WaitUtils.waitForVisible(DriverUtil.getDriver(), JsonUtils.getBy(locator))
                    .isDisplayed();
        } catch (Exception e) {
            visible = false;
        }

        Assert.assertTrue("Expected '" + labelKey + "' to be visible on page '"
                + PageContext.getCurrentPageTitle() + "'", visible);

        logger.info("  \u2714 Visible: '{}'", labelKey);
        ExtentReportManager.logPass("Visible: " + labelKey);
    }

    @Then("^\"([^\"]*)\" should display \"([^\"]*)\"$")
    public void elementShouldDisplay(String labelKey, String expectedText) {
        logger.info("STEP: Then '{}' should display '{}'", labelKey, expectedText);

        String locator = JsonUtils.jsonReaderWithNavigate(labelKey);
        String actual = WaitUtils.waitForVisible(DriverUtil.getDriver(), JsonUtils.getBy(locator))
                .getText().trim();

        Assert.assertTrue("Expected '" + labelKey + "' to contain '" + expectedText
                + "' but got '" + actual + "'", actual.contains(expectedText));

        logger.info("  \u2714 Text verified: {} = {}", labelKey, actual);
        ExtentReportManager.logPass("Text verified: " + labelKey + " = " + actual);
    }

    @Then("^I should be on the \"([^\"]*)\" page$")
    public void iShouldBeOnPage(String expectedPage) {
        logger.info("STEP: Then I should be on the '{}' page", expectedPage);

        String actual = PageContext.getCurrentPageTitle();

        if (!expectedPage.equals(actual)) {
            // Fallback: role-ambiguous pages (e.g. "My Profile" sets navigateTo to
            // "AdminProfilePage" in JSON regardless of user role). If the expected page
            // matches the current user's role, correct the in-memory context.
            String corrected = PageContext.resolveRoleAmbiguousPage(expectedPage, actual);
            if (corrected != null) {
                PageContext.setCurrentPage(corrected);
                actual = corrected;
                logger.info("  \u2714 Role-based page correction: context updated to '{}'", corrected);
            }
        }

        Assert.assertEquals("Expected page '" + expectedPage + "' but was '" + actual + "'",
                expectedPage, actual);

        logger.info("  \u2714 Page verified: '{}'", expectedPage);
        ExtentReportManager.logPass("Page verified: " + expectedPage);
    }
    
    
    @Then("^I wait for  \"([^\"]*)\" seconds to load the data$")
    public void iWaitForApplicationTOloadTheData(String seconds) throws NumberFormatException, InterruptedException {
        logger.info("STEP: Then I wait for '" + seconds+ "' seconds to load the data");
        int secnds = Integer.parseInt(seconds);
        if(secnds<5) {
        	Thread.sleep( secnds * 1000);
        } else {
        	String messageString = "Wait time can't be more than 4 seconds";
        	
		}
        
    }
    

    @When("^@([^\\s]+)$")
    public void runLibraryScenario(String tag) {
        logger.info("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        logger.info("STEP: When @{} \u2014 expanding library scenario", tag);
        List<String> steps = ScenarioLibrary.getSteps(tag);
        for (String step : steps) {
            logger.info("  \u2192 {}", step);
            StepInvoker.invoke(this, step);
        }
        logger.info("  \u2714 Library scenario @{} complete", tag);
        ExtentReportManager.logInfo("Library @" + tag + " executed");
    }


}
