package com.asrtek.automation.mobile.actions;

import com.asrtek.automation.core.PageContext;
import com.asrtek.automation.core.LocatorRegistry;
import com.asrtek.automation.enums.LocatorStrategy;
import com.asrtek.automation.exception.AutomationException;
import com.asrtek.automation.models.LocatorModel;
import com.asrtek.automation.utils.ExtentReportManager;
import com.asrtek.automation.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;

/**
 * Central dispatcher for all mobile interactions — mirrors ActionHelper exactly.
 *
 * Same locator lookup pattern (LocatorRegistry + PageContext), same ExtentReport
 * logging, same WaitUtils.
 *
 * Additional mobile-specific gestures:
 *   swipeUp / swipeDown / swipeLeft / swipeRight — W3C touch actions
 *   longPress — 2-second touch hold
 *   scrollToElement — scroll until element is visible
 *
 * Design patterns: Facade + Strategy (LocatorStrategy drives By selection)
 */
public class MobileActionHelper {

    private final AppiumDriver driver;

    public MobileActionHelper(AppiumDriver driver) {
        this.driver = driver;
    }

    // ── Core interactions (same as ActionHelper) ──────────────────────────────

    public void tap(String labelKey) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        try {
            WaitUtils.waitForClickable(driver, toBy(lm)).click();
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to tap '" + labelKey + "' on page [" + page + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Tapped '" + labelKey + "' on [" + page + "]");
        updatePage(lm);
    }

    public void tapOnPage(String pageName, String labelKey) {
        LocatorModel lm = LocatorRegistry.getInstance().find(pageName, labelKey);
        try {
            WaitUtils.waitForClickable(driver, toBy(lm)).click();
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to tap '" + labelKey + "' on page [" + pageName + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Tapped '" + labelKey + "' on [" + pageName + "]");
        updatePage(lm);
    }

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

    public boolean isVisible(String labelKey) {
        try {
            String page = PageContext.getCurrentPage();
            LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
            return WaitUtils.waitForVisible(driver, toBy(lm)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Mobile-specific gestures ──────────────────────────────────────────────

    public void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        swipe(size.width / 2, (int)(size.height * 0.75),
              size.width / 2, (int)(size.height * 0.25));
        ExtentReportManager.logInfo("Swiped UP");
    }

    public void swipeDown() {
        Dimension size = driver.manage().window().getSize();
        swipe(size.width / 2, (int)(size.height * 0.25),
              size.width / 2, (int)(size.height * 0.75));
        ExtentReportManager.logInfo("Swiped DOWN");
    }

    public void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        swipe((int)(size.width * 0.8), size.height / 2,
              (int)(size.width * 0.2), size.height / 2);
        ExtentReportManager.logInfo("Swiped LEFT");
    }

    public void swipeRight() {
        Dimension size = driver.manage().window().getSize();
        swipe((int)(size.width * 0.2), size.height / 2,
              (int)(size.width * 0.8), size.height / 2);
        ExtentReportManager.logInfo("Swiped RIGHT");
    }

    public void longPress(String labelKey) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        try {
            WebElement el = WaitUtils.waitForVisible(driver, toBy(lm));
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence longPress = new Sequence(finger, 1);
            longPress.addAction(finger.createPointerMove(Duration.ZERO,
                    PointerInput.Origin.fromElement(el), 0, 0));
            longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            longPress.addAction(finger.createPointerMove(Duration.ofSeconds(2),
                    PointerInput.Origin.fromElement(el), 0, 0));
            longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(longPress));
        } catch (Exception e) {
            throw new AutomationException(
                    "Failed to long-press '" + labelKey + "' on page [" + page + "]: " + e.getMessage(), e);
        }
        ExtentReportManager.logInfo("Long-pressed '" + labelKey + "' on [" + page + "]");
    }

    public void scrollToElement(String labelKey) {
        String page = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(page, labelKey);
        By by = toBy(lm);
        for (int i = 0; i < 5; i++) {
            try {
                if (driver.findElement(by).isDisplayed()) {
                    ExtentReportManager.logInfo("Scrolled to '" + labelKey + "'");
                    return;
                }
            } catch (Exception ignored) {}
            swipeUp();
        }
        throw new AutomationException("Element '" + labelKey + "' not found after scrolling.");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void swipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(800),
                PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
    }

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
