package com.asrtek.automation.utils;

import com.asrtek.automation.config.ConfigReader;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Singleton manager for ExtentReports.
 * Produces an HTML report at target/extent-reports/Report_<timestamp>.html
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<ExtentTest>();

    public static ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String reportPath = System.getProperty("user.dir")
                    + "/target/extent-reports/Report_" + timestamp + ".html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("ASR-TEK Portal — Test Report");
            spark.config().setReportName("Automation Test Results");
            spark.config().setEncoding("UTF-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application", "ASR-TEK Portal");
            extent.setSystemInfo("Environment", ConfigReader.getInstance().getEnvironment().toString());
            extent.setSystemInfo("Browser", ConfigReader.getInstance().getBrowser().toString());
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    public static void createTest(String testName) {
        ExtentTest test = getInstance().createTest(testName);
        testThread.set(test);
    }

    public static ExtentTest getTest() {
        return testThread.get();
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static void logInfo(String message) {
        if (testThread.get() != null) {
            testThread.get().info(message);
        }
    }

    public static void logPass(String message) {
        if (testThread.get() != null) {
            testThread.get().pass(message);
        }
    }

    public static void logFail(String message) {
        if (testThread.get() != null) {
            testThread.get().fail(message);
        }
    }
}
