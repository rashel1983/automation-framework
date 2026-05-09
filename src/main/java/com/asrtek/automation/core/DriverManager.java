package com.asrtek.automation.core;

import com.asrtek.automation.config.ConfigReader;
import com.asrtek.automation.enums.Browser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Manages WebDriver instances — one driver object per user, created only once.
 *
 * Singleton guarantee:
 *   activeSessions ensures a user's driver is created exactly once.
 *   Subsequent calls for the same user return the existing instance.
 *
 * Session reuse (config: session.reuse=true):
 *   An existing session is reused if it is alive AND not past the configured timeout.
 *   When session.reuse=false, each call always creates a fresh driver.
 *
 * Parallel support:
 *   ThreadLocal<WebDriver> holds the current thread's driver reference so
 *   parallel scenarios don't share driver state.
 *
 * Design patterns: Singleton (per user) + Factory (browser creation)
 */
public class DriverManager {

    // userName → WebDriver (one live instance per user across all scenarios)
    private static final Map<String, WebDriver> activeSessions =
            new HashMap<String, WebDriver>();

    // userName → session creation timestamp (milliseconds)
    private static final Map<String, Long> sessionCreatedAt =
            new HashMap<String, Long>();

    // Per-thread reference — parallel scenarios each get their own driver pointer
    private static final ThreadLocal<WebDriver> threadDriver = new ThreadLocal<WebDriver>();

    private DriverManager() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the driver for the given user.
     *
     * - If session.reuse=true AND a live, non-expired session exists → return it.
     * - Otherwise → create a new driver (only one per user at any point in time).
     */
    public static synchronized WebDriver getOrCreateSession(String userName) {
        ConfigReader config = ConfigReader.getInstance();

        if (config.isSessionReuse()) {
            WebDriver existing = activeSessions.get(userName);
            if (existing != null && isAlive(existing) && !isExpired(userName)) {
                System.out.println("[DriverManager] Reusing session for: " + userName);
                threadDriver.set(existing);
                return existing;
            }
            // Session dead or expired — clean up before creating a new one
            if (existing != null) {
                safeQuit(existing, userName);
            }
        }

        System.out.println("[DriverManager] Creating new driver for: " + userName
                + " [browser=" + config.getBrowser()
                + ", headless=" + config.isHeadless() + "]");

        WebDriver driver = createDriver(config.getBrowser(), config.isHeadless());
        applyTimeouts(driver, config);

        activeSessions.put(userName, driver);
        sessionCreatedAt.put(userName, System.currentTimeMillis());
        threadDriver.set(driver);
        return driver;
    }

    /**
     * Returns the WebDriver bound to the current thread.
     * Throws if no session has been started.
     */
    public static WebDriver getDriver() {
        WebDriver driver = threadDriver.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "[DriverManager] No active WebDriver on this thread. "
                    + "Call getOrCreateSession() before interacting with the browser.");
        }
        return driver;
    }

    /** Gracefully closes a single user's session. */
    public static synchronized void closeSession(String userName) {
        WebDriver driver = activeSessions.remove(userName);
        sessionCreatedAt.remove(userName);
        if (driver != null) {
            safeQuit(driver, userName);
        }
    }

    /** Closes ALL active sessions — called at end of the test suite. */
    public static synchronized void quitAll() {
        System.out.println("[DriverManager] Closing " + activeSessions.size() + " session(s).");
        for (Map.Entry<String, WebDriver> entry : activeSessions.entrySet()) {
            safeQuit(entry.getValue(), entry.getKey());
        }
        activeSessions.clear();
        sessionCreatedAt.clear();
        threadDriver.remove();
    }

    // -------------------------------------------------------------------------
    // Driver factory — switches on Browser enum
    // -------------------------------------------------------------------------

    private static WebDriver createDriver(Browser browser, boolean headless) {
        switch (browser) {
            case FIREFOX:
                return createFirefox(headless);
            case EDGE:
                return createEdge(headless);
            case CHROME:
            default:
                return createChrome(headless);
        }
    }

    private static WebDriver createChrome(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }
        FirefoxDriver driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver createEdge(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        return new EdgeDriver(options);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void applyTimeouts(WebDriver driver, ConfigReader config) {
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(config.getImplicitWait()));
        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeout()));
    }

    private static boolean isAlive(WebDriver driver) {
        try {
            driver.getCurrentUrl();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isExpired(String userName) {
        Long created = sessionCreatedAt.get(userName);
        if (created == null) return true;
        long timeoutMs = ConfigReader.getInstance().getSessionTimeoutMinutes()
                * 60L * 1000L;
        return (System.currentTimeMillis() - created) > timeoutMs;
    }

    private static void safeQuit(WebDriver driver, String userName) {
        try {
            driver.quit();
            System.out.println("[DriverManager] Closed session for: " + userName);
        } catch (Exception e) {
            System.out.println("[DriverManager] Error closing session for '"
                    + userName + "': " + e.getMessage());
        }
    }
}
