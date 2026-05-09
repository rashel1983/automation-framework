package com.asrtek.automation.core;

import com.asrtek.automation.exception.AutomationException;
import com.asrtek.automation.enums.LocatorStrategy;
import com.asrtek.automation.interfaces.IAction;
import com.asrtek.automation.models.LocatorModel;
import com.asrtek.automation.utils.ExtentReportManager;
import com.asrtek.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Central dispatcher for all UI interactions — implements IAction.
 *
 * Navigation model:
 *   1. Resolve the locator from LocatorRegistry using the current page (PageContext).
 *   2. Convert the locator string to a Selenium By via LocatorStrategy enum.
 *   3. Apply an explicit Selenium wait before every interaction.
 *   4. After acting, update PageContext if the locator defines a 'navigateTo' page.
 *
 * Design patterns: Facade + Strategy (LocatorStrategy drives By selection)
 */
public class ActionHelper implements IAction {

    private final WebDriver driver;

    public ActionHelper(WebDriver driver) {
        this.driver = driver;
    }

    // -------------------------------------------------------------------------
    // IAction implementation
    // -------------------------------------------------------------------------

    @Override
    public void click(String labelKey) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        try {
            WaitUtils.waitForClickable(driver, toBy(lm)).click();
            WaitUtils.waitForPageLoad(driver);
            WaitUtils.waitForAngular(driver);
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to click '" + labelKey + "' on page [" + page + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Clicked '" + labelKey + "' on [" + page + "]");
        updatePage(lm);
    }

    @Override
    public void clickOnPage(String pageName, String labelKey) {
        LocatorModel lm = LocatorRegistry.getInstance().find(pageName, labelKey);
        try {
            WaitUtils.waitForClickable(driver, toBy(lm)).click();
            WaitUtils.waitForPageLoad(driver);
            WaitUtils.waitForAngular(driver);
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to click '" + labelKey + "' on page [" + pageName + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Clicked '" + labelKey + "' on [" + pageName + "]");
        updatePage(lm);
    }

    @Override
    public void type(String labelKey, String value) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        try {
            WebElement el = WaitUtils.waitForVisible(driver, toBy(lm));
            el.clear();
            el.sendKeys(value);
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to type into '" + labelKey + "' on page [" + page + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Typed '" + value + "' into '" + labelKey + "' on [" + page + "]");
    }

    @Override
    public void selectDropdown(String labelKey, String visibleText) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        try {
            new Select(WaitUtils.waitForVisible(driver, toBy(lm))).selectByVisibleText(visibleText);
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to select '" + visibleText + "' in '" + labelKey + "' on page [" + page + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Selected '" + visibleText + "' in '" + labelKey + "' on [" + page + "]");
    }

    @Override
    public String getText(String labelKey) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        try {
            return WaitUtils.waitForVisible(driver, toBy(lm)).getText().trim();
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to get text of '" + labelKey + "' on page [" + page + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isVisible(String labelKey) {
        try {
            String page = PageContext.getCurrentPage();
            LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
            return WaitUtils.waitForVisible(driver, toBy(lm)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private By toBy(LocatorModel lm) {
        return LocatorStrategy.fromString(lm.getLocatorType()).toBy(lm.getLocator());
    }

    private void updatePage(LocatorModel lm) {
        if (lm.getNavigateTo() != null && !lm.getNavigateTo().isEmpty()) {
            PageContext.setCurrentPage(lm.getNavigateTo());
            ExtentReportManager.logInfo("Page context → " + lm.getNavigateTo());
        }
    }
}
