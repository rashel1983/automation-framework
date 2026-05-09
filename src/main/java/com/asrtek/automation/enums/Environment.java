package com.asrtek.automation.enums;

/**
 * Supported deployment environments.
 * Drives base URL selection in ConfigReader.
 */
public enum Environment {
	LOCAL("local"),
	DEV("dev"),
    QA("qa"),
    STAGING("staging"),
    PROD("prod");

    private final String configValue;

    Environment(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static Environment fromString(String value) {
        for (Environment e : values()) {
            if (e.configValue.equalsIgnoreCase(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                "[Environment] Unknown environment value: '" + value
                + "'. Allowed values: local, dev, qa, staging, prod");
    }
}
