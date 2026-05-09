package com.asrtek.automation.mobile.steps;

import com.asrtek.automation.core.PageContext;
import com.asrtek.automation.mobile.actions.MobileActionHelper;
import com.asrtek.automation.mobile.driver.MobileDriverManager;
import com.asrtek.automation.mobile.hooks.MobileHooks;
import com.asrtek.automation.models.UserModel;
import com.asrtek.automation.utils.JsonUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

/**
 * Mobile step definitions — mirrors CommonSteps.java exactly.
 *
 * Same step text patterns as the web steps where possible so scenarios
 * read identically for UI, mobile, and API tests.
 *
 * Mobile-specific additions: swipe, scroll, long press gestures.
 */
public class MobileSteps {

    private MobileActionHelper action() {
        return MobileHooks.getActionHelper();
    }

    // ── Authentication ────────────────────────────────────────────────────────

    @Given("I am logged in as an? {string} User on mobile")
    public void iAmLoggedInAsUserOnMobile(String userType) {
        UserModel user = JsonUtils.findUserByType(userType);
        Assert.assertNotNull("No user with type '" + userType + "' in Users.json", user);

        PageContext.setCurrentPage("MobileLoginPage");
        action().tap("Username Field");
        action().type("Username Field", user.getUserName());
        action().type("Password Field", user.getPass());
        action().tap("Sign In");
    }

    @Given("I navigate to the mobile login page")
    public void iNavigateToMobileLoginPage() {
        PageContext.setCurrentPage("MobileLoginPage");
    }

    // ── Tap interactions ──────────────────────────────────────────────────────

    @When("I tap on {string}")
    public void iTapOn(String labelKey) {
        action().tap(labelKey);
    }

    @When("I tap on {string} on mobile page {string}")
    public void iTapOnOnPage(String labelKey, String pageName) {
        action().tapOnPage(pageName, labelKey);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @When("I enter {string} into {string} field on mobile")
    public void iEnterIntoFieldOnMobile(String value, String labelKey) {
        action().type(labelKey, value);
    }

    // ── Gestures ──────────────────────────────────────────────────────────────

    @When("I swipe up")
    public void iSwipeUp() {
        action().swipeUp();
    }

    @When("I swipe down")
    public void iSwipeDown() {
        action().swipeDown();
    }

    @When("I swipe left")
    public void iSwipeLeft() {
        action().swipeLeft();
    }

    @When("I swipe right")
    public void iSwipeRight() {
        action().swipeRight();
    }

    @When("I scroll to {string}")
    public void iScrollTo(String labelKey) {
        action().scrollToElement(labelKey);
    }

    @When("I long press on {string}")
    public void iLongPressOn(String labelKey) {
        action().longPress(labelKey);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    @Then("I should see {string} on mobile")
    public void iShouldSeeOnMobile(String labelKey) {
        Assert.assertTrue(
                "Expected element '" + labelKey + "' to be visible but it was not.",
                action().isVisible(labelKey));
    }

    @Then("{string} should display {string} on mobile")
    public void shouldDisplayOnMobile(String labelKey, String expectedText) {
        String actual = action().getText(labelKey);
        Assert.assertEquals(
                "Text mismatch for '" + labelKey + "'",
                expectedText, actual);
    }

    @Then("I should be on the mobile {string} page")
    public void iShouldBeOnMobilePage(String expectedPage) {
        String actual = PageContext.getCurrentPage();
        Assert.assertTrue(
                "Expected page '" + expectedPage + "' but was '" + actual + "'",
                actual.toLowerCase().contains(expectedPage.toLowerCase()));
    }

    @And("I wait for {int} seconds")
    public void iWaitForSeconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
    }
}
