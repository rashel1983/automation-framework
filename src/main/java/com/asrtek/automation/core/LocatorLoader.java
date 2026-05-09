package com.asrtek.automation.core;

import com.asrtek.automation.models.LocatorModel;
import com.asrtek.automation.utils.JsonUtils;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Scans JSON locator files from both classpath directories and loads them
 * into LocatorRegistry (JVM memory) before any test run.
 *
 *   src/test/resources/ui-locators/     — web/Selenium page locators
 *   src/test/resources/mobile-locators/ — Appium mobile page locators
 *
 * Called once from Hooks.@Before(order=0) and MobileHooks.@Before(order=0).
 * Idempotent — only loads once per JVM regardless of how many times it is called.
 */
public class LocatorLoader {

    private static final String UI_DIR     = "ui-locators";
    private static final String MOBILE_DIR = "mobile-locators";

    private static boolean loaded = false;

    /** Load ALL locators (UI + Mobile) into the shared registry. */
    public static synchronized void loadAll() {
        if (loaded) return;
        System.out.println("[LocatorLoader] Loading all locators into JVM memory...");
        int uiCount     = loadDirectory(UI_DIR);
        int mobileCount = loadDirectory(MOBILE_DIR);
        System.out.println("[LocatorLoader] Done — " + uiCount + " UI page(s), "
                + mobileCount + " Mobile page(s) registered.");
        loaded = true;
    }

    /** Load only UI locators — useful when mobile tests should not pollute the registry. */
    public static synchronized void loadUILocators() {
        if (loaded) return;
        System.out.println("[LocatorLoader] Loading UI locators...");
        int count = loadDirectory(UI_DIR);
        System.out.println("[LocatorLoader] Done — " + count + " UI page(s) registered.");
        loaded = true;
    }

    /** Load only Mobile locators. */
    public static synchronized void loadMobileLocators() {
        if (loaded) return;
        System.out.println("[LocatorLoader] Loading Mobile locators...");
        int count = loadDirectory(MOBILE_DIR);
        System.out.println("[LocatorLoader] Done — " + count + " Mobile page(s) registered.");
        loaded = true;
    }

    /** Reset the loaded flag — used by tests that need a fresh load. */
    static synchronized void reset() {
        loaded = false;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Loads all *.json files from the given classpath directory name.
     * @return number of page files loaded
     */
    private static int loadDirectory(String dirName) {
        URL resource = LocatorLoader.class.getClassLoader().getResource(dirName);
        if (resource == null) {
            System.out.println("[LocatorLoader] WARNING: '" + dirName
                    + "' not found in classpath — skipping.");
            return 0;
        }

        File dir = new File(resource.getFile());
        File[] jsonFiles = dir.listFiles(
                (d, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("[LocatorLoader] WARNING: No JSON files in '" + dirName + "'.");
            return 0;
        }

        int count = 0;
        for (File file : jsonFiles) {
            String pageName = file.getName().replace(".json", "");
            List<LocatorModel> locators = JsonUtils.parseLocators(file.getAbsolutePath());
            LocatorRegistry.getInstance().register(pageName, locators);
            count++;
        }
        return count;
    }
}
