package com.asrtek.automation.utils;

import com.asrtek.automation.core.LocatorRegistry;
import com.asrtek.automation.core.PageContext;
import com.asrtek.automation.enums.LocatorStrategy;
import com.asrtek.automation.models.LocatorModel;
import com.asrtek.automation.models.UserModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * JSON parsing utilities using Jackson.
 *
 * Key methods for step definitions:
 *   jsonReaderWithNavigate(labelKey) — resolves the locator for labelKey on the
 *       current page, fires page-context navigation if 'navigateTo' is set,
 *       and returns an encoded "locatorType|locatorValue" string.
 *   getBy(encodedLocator) — converts that encoded string into a Selenium By.
 *
 * Encoding format: "<locatorType>|<locatorValue>", e.g. "xpath|//button[@id='ok']"
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SEPARATOR = "|";

    // -------------------------------------------------------------------------
    // Locator resolution (called by step definitions)
    // -------------------------------------------------------------------------

    /**
     * Looks up labelKey on the current page, fires 'navigateTo' if present,
     * and returns an encoded locator string for use with getBy().
     */
    public static String jsonReaderWithNavigate(String labelKey) {
        String currentPage = PageContext.getCurrentPage();
        LocatorModel lm = LocatorRegistry.getInstance().find(currentPage, labelKey);

        if (lm.getNavigateTo() != null && !lm.getNavigateTo().isEmpty()) {
            PageContext.setCurrentPage(lm.getNavigateTo());
        }

        return lm.getLocatorType() + SEPARATOR + lm.getLocator();
    }

    /** Convenience: resolves labelKey on the current page and returns a Selenium By directly. */
    public static By byFor(String labelKey) {
        return getBy(jsonReaderWithNavigate(labelKey));
    }

    /**
     * Converts the encoded string returned by jsonReaderWithNavigate() into a Selenium By.
     * Format: "<locatorType>|<locatorValue>"
     */
    public static By getBy(String encodedLocator) {
        int sep = encodedLocator.indexOf(SEPARATOR);
        if (sep < 0) {
            return By.xpath(encodedLocator);
        }
        String type = encodedLocator.substring(0, sep);
        String value = encodedLocator.substring(sep + 1);
        return LocatorStrategy.fromString(type).toBy(value);
    }

    // -------------------------------------------------------------------------
    // File parsing
    // -------------------------------------------------------------------------

    public static List<LocatorModel> parseLocators(String filePath) {
        try {
            return MAPPER.readValue(
                    new File(filePath),
                    new TypeReference<List<LocatorModel>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("[JsonUtils] Failed to parse locators from: " + filePath
                    + " — " + e.getMessage(), e);
        }
    }

    public static List<UserModel> parseUsers() {
        try {
            InputStream is = JsonUtils.class.getClassLoader()
                    .getResourceAsStream("users/Users.json");
            if (is == null) {
                throw new RuntimeException("[JsonUtils] users/Users.json not found in classpath.");
            }
            return MAPPER.readValue(is, new TypeReference<List<UserModel>>() {});
        } catch (IOException e) {
            throw new RuntimeException("[JsonUtils] Failed to parse Users.json: "
                    + e.getMessage(), e);
        }
    }

    public static UserModel findUserByType(String userType) {
        List<UserModel> users = parseUsers();
        for (UserModel user : users) {
            if (user.getUserType() != null
                    && user.getUserType().equalsIgnoreCase(userType)) {
                return user;
            }
        }
        throw new RuntimeException("[JsonUtils] No user found with userType='"
                + userType + "' in Users.json");
    }
}
