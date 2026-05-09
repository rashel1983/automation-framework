package com.asrtek.automation.interfaces;

import com.asrtek.automation.models.LocatorModel;

import java.util.List;

/**
 * Contract for any class that stores and retrieves locators.
 *
 * LocatorRegistry implements this. A test-double or alternative storage
 * (e.g. database-backed locators) can swap in without touching the rest of
 * the framework.
 */
public interface ILocatorProvider {

    /**
     * Store the locator list for a named page.
     * @param pageName  Matches the JSON filename without extension (e.g. "DashboardPage").
     * @param locators  Parsed list of LocatorModel objects.
     */
    void register(String pageName, List<LocatorModel> locators);

    /**
     * Retrieve a single locator by its labelKey from the named page.
     * Throws a descriptive RuntimeException when not found so tests fail fast.
     */
    LocatorModel find(String pageName, String labelKey);

    /** Returns true if the page has been registered. */
    boolean hasPage(String pageName);
}
