package com.asrtek.automation.interfaces;

/**
 * Contract for all UI interaction actions.
 *
 * ActionHelper implements this interface. Any alternative action strategy
 * (e.g. a mobile driver adapter, an API-backed mock) can implement the same
 * contract so steps never need to change.
 */
public interface IAction {

    /** Click an element identified by labelKey on the current page. */
    void click(String labelKey);

    /** Click an element on a specific page by name (e.g. always from DashboardPage). */
    void clickOnPage(String pageName, String labelKey);

    /** Clear and type text into an input field on the current page. */
    void type(String labelKey, String value);

    /** Select a visible option from a dropdown on the current page. */
    void selectDropdown(String labelKey, String visibleText);

    /** Return the trimmed text content of an element on the current page. */
    String getText(String labelKey);

    /** Return true if the element is visible on the current page, false otherwise. */
    boolean isVisible(String labelKey);
}
