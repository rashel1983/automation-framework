package com.asrtek.automation.config;

import com.asrtek.automation.enums.Browser;
import com.asrtek.automation.enums.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration reader backed by src/test/resources/config.properties.
 *
 * Any value can also be overridden at runtime by passing a system property:
 *   mvn test -Dbrowser=firefox -Denv=staging -Dheadless=true
 *
 * Priority: system property > config.properties default.
 *
 * Design pattern: Singleton (thread-safe via synchronized init)
 */
public class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static ConfigReader instance;
    private final Properties properties = new Properties();

    private ConfigReader() {
        java.net.URL url = getClass().getClassLoader().getResource(CONFIG_FILE);
        if (url == null) {
            throw new RuntimeException("[ConfigReader] '" + CONFIG_FILE
                    + "' not found in classpath. "
                    + "Ensure it exists at src/test/resources/config.properties");
        }
        System.out.println("[ConfigReader] Loading from: " + url.getPath());
        try (InputStream stream = url.openStream()) {
            properties.load(stream);
            System.out.println("[ConfigReader] Loaded configuration from " + CONFIG_FILE);
        } catch (IOException e) {
            throw new RuntimeException("[ConfigReader] Failed to load " + CONFIG_FILE, e);
        }
    }

    public static synchronized ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Raw property access — config.properties is the single source of truth
    // -------------------------------------------------------------------------

    public String get(String key) {
        String fileValue = properties.getProperty(key);
        if (fileValue != null && !fileValue.trim().isEmpty()) {
            return fileValue.trim();
        }
        throw new RuntimeException("[ConfigReader] Key '" + key
                + "' not found in " + CONFIG_FILE + ".");
    }

    public String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    // -------------------------------------------------------------------------
    // Typed accessors
    // -------------------------------------------------------------------------

    public Browser getBrowser() {
        return Browser.fromString(get("browser", "chrome"));
    }

    public Environment getEnvironment() {
        return Environment.fromString(get("env", "qa"));
    }

    /** Returns the frontend base URL for the currently configured environment. */
    public String getBaseUrl() {
        String envKey = "url." + getEnvironment().getConfigValue();
        return get(envKey);
    }

    /** Returns the API base URL (Spring Boot /api) for the currently configured environment. */
    public String getApiBaseUrl() {
        String envKey = "api.url." + getEnvironment().getConfigValue();
        return get(envKey);
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "false"));
    }

    public int getImplicitWait() {
        return Integer.parseInt(get("browser.implicit.wait", "10"));
    }

    public int getPageLoadTimeout() {
        return Integer.parseInt(get("browser.page.load.timeout", "60"));
    }

    public boolean isScreenshotOnFailure() {
        return Boolean.parseBoolean(get("screenshot.on.failure", "true"));
    }

    public boolean isSessionReuse() {
        return Boolean.parseBoolean(get("session.reuse", "true"));
    }

    public int getSessionTimeoutMinutes() {
        return Integer.parseInt(get("session.timeout.minutes", "30"));
    }

    public int getParallelThreadCount() {
        return Integer.parseInt(get("parallel.thread.count", "1"));
    }

    // -------------------------------------------------------------------------
    // Debug helper
    // -------------------------------------------------------------------------

    public void printConfig() {
        System.out.println("========================================");
        System.out.println("[ConfigReader] Active Configuration (source: " + CONFIG_FILE + "):");
        System.out.println("  browser          = " + getBrowser()
                + "  [raw: " + properties.getProperty("browser", "<not set>") + "]");
        System.out.println("  environment      = " + getEnvironment()
                + "  [raw: " + properties.getProperty("env", "<not set>") + "]");
        System.out.println("  baseUrl          = " + getBaseUrl());
        System.out.println("  headless         = " + isHeadless()
                + "  [raw: " + properties.getProperty("headless", "<not set>") + "]");
        System.out.println("  implicit.wait    = " + getImplicitWait() + "s");
        System.out.println("  page.load        = " + getPageLoadTimeout() + "s");
        System.out.println("  screenshot.fail  = " + isScreenshotOnFailure());
        System.out.println("  session.reuse    = " + isSessionReuse());
        System.out.println("  session.timeout  = " + getSessionTimeoutMinutes() + "m");
        System.out.println("  parallel.threads = " + getParallelThreadCount());
        System.out.println("========================================");
    }
}
