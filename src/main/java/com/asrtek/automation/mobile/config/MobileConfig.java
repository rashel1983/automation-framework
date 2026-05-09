package com.asrtek.automation.mobile.config;

import com.asrtek.automation.config.ConfigReader;
import com.asrtek.automation.enums.Platform;

/**
 * Mobile-specific configuration — reads from the same config.properties
 * as the rest of the framework (mobile.* keys).
 *
 * Priority: -D system property > config.properties > hardcoded default.
 * Example override: mvn test -Dtest=MobileTestRunner -Dmobile.platform=ios
 */
public final class MobileConfig {

    public static Platform getPlatform() {
        return Platform.fromString(sys("mobile.platform",
                ConfigReader.getInstance().get("mobile.platform", "android")));
    }

    public static String getPlatformVersion() {
        return sys("mobile.platform.version",
                ConfigReader.getInstance().get("mobile.platform.version", "13"));
    }

    public static String getDeviceName() {
        return sys("mobile.device.name",
                ConfigReader.getInstance().get("mobile.device.name", "emulator-5554"));
    }

    /** web = test in a mobile browser | native = test an installed app */
    public static String getMobileType() {
        return sys("mobile.type",
                ConfigReader.getInstance().get("mobile.type", "web"));
    }

    public static String getAppPath() {
        return sys("mobile.app",
                ConfigReader.getInstance().get("mobile.app", ""));
    }

    public static String getAppiumUrl() {
        return sys("mobile.appium.url",
                ConfigReader.getInstance().get("mobile.appium.url", "http://localhost:4723"));
    }

    public static boolean isNoReset() {
        return Boolean.parseBoolean(sys("mobile.no.reset",
                ConfigReader.getInstance().get("mobile.no.reset", "true")));
    }

    public static int getImplicitWait() {
        return Integer.parseInt(sys("mobile.implicit.wait",
                ConfigReader.getInstance().get("mobile.implicit.wait", "10")));
    }

    /** Returns the base URL of the web app — used when mobile.type=web */
    public static String getBaseUrl() {
        return ConfigReader.getInstance().getBaseUrl();
    }

    private static String sys(String key, String fallback) {
        String v = System.getProperty(key);
        return (v != null && !v.isEmpty()) ? v : fallback;
    }

    private MobileConfig() {}
}
