package com.asrtek.automation.enums;

/**
 * Supported browser types.
 * Used by ConfigReader and DriverManager to decide which WebDriver to instantiate.
 */
public enum Browser {
    CHROME("chrome"),
    FIREFOX("firefox"),
    EDGE("edge");

    private final String configValue;

    Browser(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    /**
     * Parse a config string value (case-insensitive) to the enum constant.
     */
    public static Browser fromString(String value) {
        for (Browser b : values()) {
            if (b.configValue.equalsIgnoreCase(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException(
                "[Browser] Unknown browser value: '" + value
                + "'. Allowed values: chrome, firefox, edge");
    }
}
