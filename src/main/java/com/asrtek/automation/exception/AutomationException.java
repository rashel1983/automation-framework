package com.asrtek.automation.exception;

/**
 * Unchecked exception for automation framework failures.
 * Wraps low-level Selenium or locator errors with context about what action
 * was being attempted, so test failures are self-explanatory.
 */
public class AutomationException extends RuntimeException {

    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}
