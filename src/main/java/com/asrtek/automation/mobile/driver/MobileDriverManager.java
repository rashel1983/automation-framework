package com.asrtek.automation.mobile.driver;

import com.asrtek.automation.enums.Platform;
import com.asrtek.automation.mobile.config.MobileConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages AppiumDriver instances — mirrors DriverManager exactly.
 *
 * Same patterns:
 *   - One driver per user (singleton per user key)
 *   - ThreadLocal for parallel scenario isolation
 *   - Session reuse with expiry check
 *   - Factory pattern: Android vs iOS driver creation
 *
 * Design patterns: Singleton (per user) + Factory (platform selection)
 */
public class MobileDriverManager {

    private static final Map<String, AppiumDriver> activeSessions = new HashMap<>();
    private static final Map<String, Long>         sessionCreatedAt = new HashMap<>();
    private static final ThreadLocal<AppiumDriver> threadDriver = new ThreadLocal<>();

    private MobileDriverManager() {}

    // ── Public API ────────────────────────────────────────────────────────────

    public static synchronized AppiumDriver getOrCreateSession(String userName) {
        AppiumDriver existing = activeSessions.get(userName);

        if (existing != null && isAlive(existing) && !isExpired(userName)) {
            System.out.println("[MobileDriverManager] Reusing session for: " + userName);
            threadDriver.set(existing);
            return existing;
        }

        if (existing != null) safeQuit(existing, userName);

        System.out.println("[MobileDriverManager] Creating new driver for: " + userName
                + " [platform=" + MobileConfig.getPlatform()
                + ", type=" + MobileConfig.getMobileType() + "]");

        AppiumDriver driver = createDriver(MobileConfig.getPlatform());
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(MobileConfig.getImplicitWait()));

        activeSessions.put(userName, driver);
        sessionCreatedAt.put(userName, System.currentTimeMillis());
        threadDriver.set(driver);
        return driver;
    }

    public static AppiumDriver getDriver() {
        AppiumDriver driver = threadDriver.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "[MobileDriverManager] No active AppiumDriver on this thread. "
                    + "Call getOrCreateSession() first.");
        }
        return driver;
    }

    public static synchronized void closeSession(String userName) {
        AppiumDriver driver = activeSessions.remove(userName);
        sessionCreatedAt.remove(userName);
        if (driver != null) safeQuit(driver, userName);
    }

    public static synchronized void quitAll() {
        System.out.println("[MobileDriverManager] Closing " + activeSessions.size() + " session(s).");
        activeSessions.forEach((user, driver) -> safeQuit(driver, user));
        activeSessions.clear();
        sessionCreatedAt.clear();
        threadDriver.remove();
    }

    // ── Driver factory ────────────────────────────────────────────────────────

    private static AppiumDriver createDriver(Platform platform) {
        switch (platform) {
            case IOS:    return createIOSDriver();
            case ANDROID:
            default:     return createAndroidDriver();
        }
    }

    private static AppiumDriver createAndroidDriver() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName(MobileConfig.getDeviceName());
        options.setPlatformVersion(MobileConfig.getPlatformVersion());
        options.setNoReset(MobileConfig.isNoReset());

        if ("web".equalsIgnoreCase(MobileConfig.getMobileType())) {
            options.withBrowserName("Chrome");
        } else {
            String appPath = MobileConfig.getAppPath();
            if (appPath == null || appPath.isEmpty()) {
                throw new IllegalStateException(
                        "[MobileDriverManager] mobile.app is required for native testing. "
                        + "Set it in config.properties or via -Dmobile.app=/path/to/app.apk");
            }
            options.setApp(appPath);
        }

        try {
            return new AndroidDriver(new URL(MobileConfig.getAppiumUrl()), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("[MobileDriverManager] Invalid Appium URL: "
                    + MobileConfig.getAppiumUrl(), e);
        }
    }

    private static AppiumDriver createIOSDriver() {
        XCUITestOptions options = new XCUITestOptions();
        options.setDeviceName(MobileConfig.getDeviceName());
        options.setPlatformVersion(MobileConfig.getPlatformVersion());
        options.setNoReset(MobileConfig.isNoReset());

        if ("web".equalsIgnoreCase(MobileConfig.getMobileType())) {
            options.withBrowserName("Safari");
        } else {
            String appPath = MobileConfig.getAppPath();
            if (appPath == null || appPath.isEmpty()) {
                throw new IllegalStateException(
                        "[MobileDriverManager] mobile.app is required for native testing.");
            }
            options.setApp(appPath);
        }

        try {
            return new IOSDriver(new URL(MobileConfig.getAppiumUrl()), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("[MobileDriverManager] Invalid Appium URL: "
                    + MobileConfig.getAppiumUrl(), e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isAlive(AppiumDriver driver) {
        try {
            driver.getPageSource();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isExpired(String userName) {
        Long created = sessionCreatedAt.get(userName);
        if (created == null) return true;
        long timeoutMs = 30L * 60L * 1000L;
        return (System.currentTimeMillis() - created) > timeoutMs;
    }

    private static void safeQuit(AppiumDriver driver, String userName) {
        try {
            driver.quit();
            System.out.println("[MobileDriverManager] Closed session for: " + userName);
        } catch (Exception e) {
            System.out.println("[MobileDriverManager] Error closing session '"
                    + userName + "': " + e.getMessage());
        }
    }
}
