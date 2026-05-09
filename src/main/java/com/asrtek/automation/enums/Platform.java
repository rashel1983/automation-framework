package com.asrtek.automation.enums;

public enum Platform {

    ANDROID("android"),
    IOS("ios");

    private final String configValue;

    Platform(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static Platform fromString(String value) {
        for (Platform p : values()) {
            if (p.configValue.equalsIgnoreCase(value)) return p;
        }
        throw new IllegalArgumentException(
                "[Platform] Unknown platform '" + value + "'. Use: android | ios");
    }
}
