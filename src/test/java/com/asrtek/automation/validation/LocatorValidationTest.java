package com.asrtek.automation.validation;

import com.asrtek.automation.models.LocatorModel;
import com.asrtek.automation.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Validates every locator JSON file in ui-locators/ and mobile-locators/
 * without opening a browser.
 *
 * Rules enforced per locator entry:
 *   1. labelkey  — present and non-blank
 *   2. locator   — present and non-blank
 *   3. locatorType — when present, must be one of the supported strategies
 *   4. No duplicate labelkey within the same page file
 *   5. XPath expressions must be syntactically valid (validated via XPathFactory)
 *
 * Run:
 *   mvn test -Dtest=LocatorValidationTest
 */
public class LocatorValidationTest {

    private static final Set<String> VALID_TYPES = new HashSet<>(Arrays.asList(
            "xpath", "css", "id", "name", "class", "linktext"
    ));

    // ── Test entry points ─────────────────────────────────────────────────────

    @Test
    public void validateUILocators() {
        List<String> violations = validateDirectory("ui-locators");
        assertNoViolations("UI Locators", violations);
    }

    @Test
    public void validateMobileLocators() {
        List<String> violations = validateDirectory("mobile-locators");
        assertNoViolations("Mobile Locators", violations);
    }

    // ── Core validation logic ─────────────────────────────────────────────────

    private List<String> validateDirectory(String dirName) {
        List<String> violations = new ArrayList<>();

        URL resource = getClass().getClassLoader().getResource(dirName);
        if (resource == null) {
            violations.add("[" + dirName + "] Directory not found in classpath.");
            return violations;
        }

        File dir = new File(resource.getFile());
        File[] jsonFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            violations.add("[" + dirName + "] No JSON files found.");
            return violations;
        }

        // Sort for deterministic output
        Arrays.sort(jsonFiles, Comparator.comparing(File::getName));

        for (File file : jsonFiles) {
            violations.addAll(validateFile(file, dirName));
        }

        return violations;
    }

    private List<String> validateFile(File file, String dirName) {
        List<String> violations = new ArrayList<>();
        String pageName = file.getName().replace(".json", "");
        String location = dirName + "/" + file.getName();

        List<LocatorModel> locators;
        try {
            locators = JsonUtils.parseLocators(file.getAbsolutePath());
        } catch (Exception e) {
            violations.add("[" + location + "] Failed to parse JSON: " + e.getMessage());
            return violations;
        }

        if (locators == null || locators.isEmpty()) {
            violations.add("[" + location + "] File is empty or has no locator entries.");
            return violations;
        }

        Set<String> seenKeys = new LinkedHashSet<>();

        for (int i = 0; i < locators.size(); i++) {
            LocatorModel lm = locators.get(i);
            String entry = "[" + location + "] entry[" + i + "]";

            // Rule 1 — labelkey required and non-blank
            if (lm.getLabelkey() == null || lm.getLabelkey().trim().isEmpty()) {
                violations.add(entry + ": 'labelkey' is missing or blank.");
            }

            // Rule 2 — locator required and non-blank
            if (lm.getLocator() == null || lm.getLocator().trim().isEmpty()) {
                violations.add(entry + " (labelkey='" + lm.getLabelkey()
                        + "'): 'locator' is missing or blank.");
            }

            // Rule 3 — locatorType must be valid when present
            String type = lm.getLocatorType();
            if (type != null && !type.trim().isEmpty()) {
                if (!VALID_TYPES.contains(type.trim().toLowerCase())) {
                    violations.add(entry + " (labelkey='" + lm.getLabelkey()
                            + "'): invalid locatorType '" + type + "'. "
                            + "Allowed: " + VALID_TYPES);
                }
            }

            // Rule 4 — no duplicate labelkey within the same page
            if (lm.getLabelkey() != null && !lm.getLabelkey().trim().isEmpty()) {
                String key = lm.getLabelkey().trim().toLowerCase();
                if (!seenKeys.add(key)) {
                    violations.add(entry + ": duplicate labelkey '"
                            + lm.getLabelkey() + "' in " + location);
                }
            }

            // Rule 5 — validate XPath syntax for xpath type (default when absent)
            boolean isXpath = type == null || type.trim().isEmpty()
                    || "xpath".equalsIgnoreCase(type.trim());
            if (isXpath && lm.getLocator() != null && !lm.getLocator().trim().isEmpty()) {
                String xpathError = validateXPath(lm.getLocator().trim());
                if (xpathError != null) {
                    violations.add(entry + " (labelkey='" + lm.getLabelkey()
                            + "'): invalid XPath expression — " + xpathError
                            + "  |  xpath: " + lm.getLocator());
                }
            }
        }

        if (violations.isEmpty()) {
            System.out.println("[LocatorValidationTest] ✔  " + location
                    + " — " + locators.size() + " locator(s) valid.");
        }

        return violations;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String validateXPath(String expression) {
        try {
            XPathFactory.newInstance().newXPath().compile(expression);
            return null;
        } catch (XPathExpressionException e) {
            return e.getMessage();
        }
    }

    private void assertNoViolations(String label, List<String> violations) {
        if (violations.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n══════════════════════════════════════════\n");
        sb.append("  LOCATOR VALIDATION FAILURES — ").append(label).append("\n");
        sb.append("══════════════════════════════════════════\n");
        for (int i = 0; i < violations.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(violations.get(i)).append("\n");
        }
        sb.append("══════════════════════════════════════════\n");
        sb.append("  Total violations: ").append(violations.size()).append("\n");
        sb.append("══════════════════════════════════════════\n");

        Assert.fail(sb.toString());
    }
}
