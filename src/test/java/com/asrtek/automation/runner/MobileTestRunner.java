package com.asrtek.automation.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * 
 * Prerequisites Before Running

# 1. Install Appium
npm install -g appium

# 2. Install UIAutomator2 driver (Android)
appium driver install uiautomator2

# 3. Start Appium server
appium

# 4. Start Android emulator (or connect a device)
emulator -avd Pixel_7_API_33


 * Mobile-only test runner — does NOT start a desktop browser.
 * Requires Appium server running on mobile.appium.url (default: http://localhost:4723)
 * and a connected device or emulator.
 *
 * Run all mobile tests:
 *   mvn test -Dtest=MobileTestRunner
 *
 * Run only smoke mobile tests:
 *   mvn test -Dtest=MobileTestRunner -Dcucumber.filter.tags="@smoke"
 *
 * Run on iOS instead of Android:
 *   mvn test -Dtest=MobileTestRunner -Dmobile.platform=ios -Dmobile.device.name="iPhone 15"
 *
 * Run native app tests:
 *   mvn test -Dtest=MobileTestRunner -Dmobile.type=native -Dmobile.app=/path/to/app.apk
 *
 * Prerequisites:
 *   1. Appium server running:   npx appium
 *   2. Android emulator running OR physical device connected via USB
 *   3. UIAutomator2 driver installed: appium driver install uiautomator2
 *      (iOS) XCUITest driver:         appium driver install xcuitest
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features  = "src/test/resources/features/mobile",
        glue      = {
                "com.asrtek.automation.mobile.hooks",
                "com.asrtek.automation.mobile.steps"
        },
        tags      = "not @wip",
        plugin    = {
                "pretty",
                "html:target/cucumber-reports/mobile/cucumber-report.html",
                "json:target/cucumber-reports/mobile/cucumber-report.json"
        },
        monochrome = true
)
public class MobileTestRunner {}
