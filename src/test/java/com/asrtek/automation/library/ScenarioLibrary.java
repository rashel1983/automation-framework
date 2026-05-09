package com.asrtek.automation.library;

import com.asrtek.automation.exception.AutomationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parses all .feature files under src/test/resources/library/ and indexes
 * each scenario's steps by its tag.
 *
 * Usage:
 *   List<String> steps = ScenarioLibrary.getSteps("addemployee");
 *   // → ["I am logged in as an \"Test User\" User", "I click on \"Employees\" ..."...]
 */
public class ScenarioLibrary {

    private static final String LIBRARY_DIR = "src/test/resources/library";
    private static final Map<String, List<String>> REGISTRY = new HashMap<>();
    private static volatile boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;
        File dir = new File(LIBRARY_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new AutomationException(
                    "[ScenarioLibrary] Library directory not found: " + dir.getAbsolutePath());
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".feature"));
        if (files != null) {
            for (File f : files) {
                parse(f);
            }
        }
        loaded = true;
        System.out.println("[ScenarioLibrary] Loaded tags: " + REGISTRY.keySet());
    }

    public static List<String> getSteps(String tag) {
        load();
        List<String> steps = REGISTRY.get(tag.toLowerCase());
        if (steps == null) {
            throw new AutomationException(
                    "[ScenarioLibrary] No scenario found for tag: @" + tag
                    + ". Available: " + REGISTRY.keySet());
        }
        return Collections.unmodifiableList(steps);
    }

    private static void parse(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String pendingTag = null;
            List<String> currentSteps = null;
            boolean inScenario = false;

            while ((line = reader.readLine()) != null) {
                String t = line.trim();

                if (t.isEmpty()) {
                    saveIfReady(pendingTag, currentSteps);
                    if (inScenario) {
                        pendingTag = null;
                        currentSteps = null;
                        inScenario = false;
                    }
                    continue;
                }

                // Feature/Background lines reset scenario state
                if (t.startsWith("Feature:") || t.startsWith("Background:")) {
                    pendingTag = null;
                    currentSteps = null;
                    inScenario = false;
                    continue;
                }

                // Tags outside a scenario body are candidate scenario tags
                if (t.startsWith("@") && !inScenario) {
                    pendingTag = t.substring(1).toLowerCase();
                    continue;
                }

                if (t.startsWith("Scenario:")) {
                    currentSteps = new ArrayList<>();
                    inScenario = true;
                    continue;
                }

                if (inScenario && isStep(t)) {
                    currentSteps.add(stripKeyword(t));
                }
            }

            // EOF — save the last scenario
            saveIfReady(pendingTag, currentSteps);

        } catch (IOException e) {
            throw new AutomationException(
                    "[ScenarioLibrary] Failed to parse: " + file.getName() + " — " + e.getMessage(), e);
        }
    }

    private static void saveIfReady(String tag, List<String> steps) {
        if (tag != null && steps != null && !steps.isEmpty()) {
            REGISTRY.put(tag, new ArrayList<>(steps));
        }
    }

    private static boolean isStep(String line) {
        return line.startsWith("Given ") || line.startsWith("When ")
                || line.startsWith("Then ") || line.startsWith("And ")
                || line.startsWith("But ");
    }

    private static String stripKeyword(String line) {
        for (String kw : new String[]{"Given ", "When ", "Then ", "And ", "But "}) {
            if (line.startsWith(kw)) return line.substring(kw.length());
        }
        return line;
    }
}
