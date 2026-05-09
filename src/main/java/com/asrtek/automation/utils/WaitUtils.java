package com.asrtek.automation.utils;

import com.asrtek.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Centralised Selenium wait utilities.
 * Timeouts are read from ConfigReader (config.properties) — no magic numbers.
 * All ActionHelper interactions go through these methods.
 */
public class WaitUtils {

    private WaitUtils() {}

    private static int defaultTimeout() {
        return ConfigReader.getInstance().getImplicitWait();
    }

    private static int pageLoadTimeout() {
        return ConfigReader.getInstance().getPageLoadTimeout();
    }

    // -------------------------------------------------------------------------
    // Element waits
    // -------------------------------------------------------------------------

    public static WebElement waitForVisible(WebDriver driver, By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout()))
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForClickable(WebDriver driver, By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout()))
                .until(ExpectedConditions.elementToBeClickable(by));
    }

    public static WebElement waitForPresence(WebDriver driver, By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout()))
                .until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static boolean waitForText(WebDriver driver, By by, String text) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout()))
                .until(ExpectedConditions.textToBePresentInElementLocated(by, text));
    }

    public static boolean waitForInvisibility(WebDriver driver, By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout()))
                .until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    // -------------------------------------------------------------------------
    // Page-level waits
    // -------------------------------------------------------------------------

    /** Wait until the browser reports document.readyState === 'complete'. */
    public static void waitForPageLoad(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(pageLoadTimeout()))
                .until(d -> "complete".equals(
                        ((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    /**
     * Wait until Angular's test-stability API reports no pending async work.
     * Falls back gracefully on non-Angular pages.
     */
    public static void waitForAngular(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(pageLoadTimeout()))
                    .until(d -> {
                        Object stable = ((JavascriptExecutor) d).executeScript(
                                "return (typeof window.getAllAngularTestabilities === 'function')"
                                + " ? window.getAllAngularTestabilities()"
                                + "       .every(function(t){ return t.isStable(); })"
                                + " : true;");
                        return Boolean.TRUE.equals(stable);
                    });
        } catch (Exception ignored) {
            // non-Angular page or testabilities unavailable — continue
        }
    }
}
