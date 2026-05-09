package com.asrtek.automation.core;

import org.openqa.selenium.WebDriver;

/**
 * Static facade over DriverManager — lets step definitions access the current
 * thread's driver without holding an instance field.
 */
public class DriverUtil {

    private DriverUtil() {}

    public static WebDriver getDriver() {
        return DriverManager.getDriver();
    }
}
