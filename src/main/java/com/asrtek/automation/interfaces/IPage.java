package com.asrtek.automation.interfaces;

import org.openqa.selenium.WebDriver;

/**
 * Contract for any Page Object that wraps a specific application page.
 *
 * In this JSON-driven framework pages are data (JSON files), not Java classes.
 * IPage exists so that specialised pages (e.g. LoginPage with extra login logic)
 * can be implemented as typed objects that still integrate cleanly with
 * PageContext and ActionHelper.
 */
public interface IPage {

    /** The page name that maps to a JSON locator file (e.g. "DashboardPage"). */
    String getPageName();

    /**
     * Assert that the browser is currently on this page.
     * Implementations typically check a heading, URL segment, or unique element.
     *
     * @return true if the page is loaded and ready for interaction.
     */
    boolean isLoaded(WebDriver driver);

    /**
     * Navigate the browser to this page's URL (if the page has a direct URL).
     * May be a no-op for pages that are only reachable via navigation flows.
     */
    void navigateTo(WebDriver driver);
}
