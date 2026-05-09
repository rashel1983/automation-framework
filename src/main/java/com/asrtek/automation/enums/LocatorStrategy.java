package com.asrtek.automation.enums;

import org.openqa.selenium.By;

/**
 * Supported Selenium locator strategies.
 * Each constant knows how to build the corresponding By object — ActionHelper
 * calls toBy(value) instead of hard-coding By.xpath everywhere.
 *
 * JSON locator files specify the strategy via the optional "locatorType" field.
 * Default is XPATH when the field is absent.
 */
public enum LocatorStrategy {

    XPATH("xpath") {
        @Override
        public By toBy(String value) { return By.xpath(value); }
    },
    CSS("css") {
        @Override
        public By toBy(String value) { return By.cssSelector(value); }
    },
    ID("id") {
        @Override
        public By toBy(String value) { return By.id(value); }
    },
    NAME("name") {
        @Override
        public By toBy(String value) { return By.name(value); }
    },
    CLASS("class") {
        @Override
        public By toBy(String value) { return By.className(value); }
    },
    LINK_TEXT("linkText") {
        @Override
        public By toBy(String value) { return By.linkText(value); }
    };

    private final String configValue;

    LocatorStrategy(String configValue) {
        this.configValue = configValue;
    }

    /** Build the Selenium By object from the locator string value. */
    public abstract By toBy(String value);

    public static LocatorStrategy fromString(String value) {
        if (value == null || value.isEmpty()) {
            return XPATH; // safe default
        }
        for (LocatorStrategy s : values()) {
            if (s.configValue.equalsIgnoreCase(value)) {
                return s;
            }
        }
        return XPATH; // fall back to xpath for unknown values
    }
}
