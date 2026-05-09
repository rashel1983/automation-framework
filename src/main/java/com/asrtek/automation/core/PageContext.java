package com.asrtek.automation.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks which page the browser is currently on and keeps a navigation path
 * so step logs can show the full journey (e.g. LoginPage → DashboardPage → StudentsPage).
 *
 * ThreadLocal ensures parallel test runs don't share state.
 */
public class PageContext {

    private static final ThreadLocal<String> currentPage =
            new ThreadLocal<String>() {
                @Override
                protected String initialValue() { return "LoginPage"; }
            };

    private static final ThreadLocal<List<String>> navigationPath =
            new ThreadLocal<List<String>>() {
                @Override
                protected List<String> initialValue() {
                    List<String> path = new ArrayList<String>();
                    path.add("LoginPage");
                    return path;
                }
            };

    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<String>();

    public static String getCurrentPage() {
        return currentPage.get();
    }

    /** Alias used by step-level logging — same as getCurrentPage(). */
    public static String getCurrentPageTitle() {
        return currentPage.get();
    }

    public static void setCurrentPage(String pageName) {
        currentPage.set(pageName);
        navigationPath.get().add(pageName);
    }

    /** Returns the full navigation trail for the current thread, e.g. "LoginPage → DashboardPage". */
    public static String getNavigationPath() {
        List<String> path = navigationPath.get();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" \u2192 ");
            sb.append(path.get(i));
        }
        return sb.toString();
    }

    public static void setCurrentUserRole(String role) {
        currentUserRole.set(role);
    }

    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }

    /**
     * Returns the corrected page name when navigateTo in JSON doesn't distinguish by user role.
     * Returns null when no role-based correction applies.
     */
    public static String resolveRoleAmbiguousPage(String expectedPage, String actualPage) {
        String role = currentUserRole.get();
        if ("app-user".equals(role)
                && "ProfilePage".equals(expectedPage)
                && "AdminProfilePage".equals(actualPage)) {
            return "ProfilePage";
        }
        return null;
    }

    public static void reset() {
        currentPage.set("LoginPage");
        navigationPath.remove();
        currentUserRole.remove();
    }
}
