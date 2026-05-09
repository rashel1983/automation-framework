package com.asrtek.automation.models;

/**
 * Represents one locator entry from a page JSON file.
 *
 * Fields:
 *   labelkey     — human-readable key used in step definitions ("Sign In", "Username")
 *   locatorType  — strategy to use: xpath | css | id | name | class | linkText (default: xpath)
 *   locator      — the actual locator string ("//*[contains(text(),'Sign In')]")
 *   navigateTo   — page name to set in PageContext after interacting with this element
 *   description  — free-text documentation for the locator
 */
public class LocatorModel {

    private String labelkey;
    private String locatorType = "xpath";
    private String locator;
    private String navigateTo;
    private String description;

    public LocatorModel() {}

    public LocatorModel(String labelkey, String locatorType, String locator,
                        String navigateTo, String description) {
        this.labelkey = labelkey;
        this.locatorType = (locatorType != null && !locatorType.isEmpty()) ? locatorType : "xpath";
        this.locator = locator;
        this.navigateTo = navigateTo;
        this.description = description;
    }

    public String getLabelkey() { return labelkey; }
    public void setLabelkey(String labelkey) { this.labelkey = labelkey; }

    public String getLocatorType() { return locatorType; }
    public void setLocatorType(String locatorType) {
        this.locatorType = (locatorType != null && !locatorType.isEmpty()) ? locatorType : "xpath";
    }

    public String getLocator() { return locator; }
    public void setLocator(String locator) { this.locator = locator; }

    public String getNavigateTo() { return navigateTo; }
    public void setNavigateTo(String navigateTo) { this.navigateTo = navigateTo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "LocatorModel{labelkey='" + labelkey + "', locatorType='" + locatorType
                + "', locator='" + locator + "', navigateTo='" + navigateTo + "'}";
    }
}
