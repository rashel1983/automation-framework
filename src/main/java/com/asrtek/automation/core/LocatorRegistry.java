package com.asrtek.automation.core;

import com.asrtek.automation.exception.AutomationException;
import com.asrtek.automation.interfaces.ILocatorProvider;
import com.asrtek.automation.models.LocatorModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton in-JVM registry that holds ALL page locators loaded at startup.
 *
 * Implements ILocatorProvider so any component that needs locators depends
 * on the interface, not the concrete class (Dependency Inversion Principle).
 *
 * Design pattern: Singleton
 */
public class LocatorRegistry implements ILocatorProvider {

    private static LocatorRegistry instance;

    // Map<PageName, List<LocatorModel>> — all pages in JVM memory after startup
    private final Map<String, List<LocatorModel>> pageLocators =
            new HashMap<String, List<LocatorModel>>();

    private LocatorRegistry() {}

    public static synchronized LocatorRegistry getInstance() {
        if (instance == null) {
            instance = new LocatorRegistry();
        }
        return instance;
    }

    @Override
    public void register(String pageName, List<LocatorModel> locators) {
        pageLocators.put(pageName, locators);
        System.out.println("[LocatorRegistry] Registered '" + pageName
                + "' — " + locators.size() + " locator(s).");
    }

    @Override
    public LocatorModel find(String pageName, String labelKey) {
        List<LocatorModel> locators = pageLocators.get(pageName);
        if (locators == null) {
            throw new AutomationException(
                    "[LocatorRegistry] Page '" + pageName + "' not found in registry. "
                    + "Check that '" + pageName + ".json' exists in "
                    + "src/test/resources/ui-locators/ (web) or "
                    + "src/test/resources/mobile-locators/ (mobile).");
        }
        for (LocatorModel lm : locators) {
            if (lm.getLabelkey() != null
                    && lm.getLabelkey().equalsIgnoreCase(labelKey)) {
                return lm;
            }
        }
        throw new AutomationException(
                "[LocatorRegistry] labelKey='" + labelKey + "' not found in page '"
                + pageName + "'. Available keys: " + keys(locators));
    }

    @Override
    public boolean hasPage(String pageName) {
        return pageLocators.containsKey(pageName);
    }

    public Map<String, List<LocatorModel>> getAllPages() {
        return pageLocators;
    }

    private String keys(List<LocatorModel> locators) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < locators.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(locators.get(i).getLabelkey());
        }
        sb.append("]");
        return sb.toString();
    }
}
