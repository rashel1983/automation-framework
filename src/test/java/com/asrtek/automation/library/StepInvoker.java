package com.asrtek.automation.library;

import com.asrtek.automation.exception.AutomationException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reflectively invokes a step definition method on a step class instance
 * by matching the step text against @Given / @When / @Then regex patterns.
 *
 * Usage:
 *   StepInvoker.invoke(this, "I click on \"Employees\" option in side menu");
 */
public class StepInvoker {

    public static void invoke(Object stepDefinitions, String stepText) {
        for (Method method : stepDefinitions.getClass().getMethods()) {
            String pattern = extractPattern(method);
            if (pattern == null) continue;

            Matcher matcher = Pattern.compile(pattern).matcher(stepText);
            if (!matcher.matches()) continue;

            Object[] args = buildArgs(method.getParameterTypes(), matcher);
            try {
                method.invoke(stepDefinitions, args);
                return;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new AutomationException(
                        "[StepInvoker] Library step failed: \"" + stepText + "\" — " + cause.getMessage(), cause);
            } catch (IllegalAccessException e) {
                throw new AutomationException(
                        "[StepInvoker] Cannot access step method for: \"" + stepText + "\"", e);
            }
        }
        throw new AutomationException(
                "[StepInvoker] No step definition matched: \"" + stepText + "\"");
    }

    private static String extractPattern(Method method) {
        When when = method.getAnnotation(When.class);
        if (when != null) return when.value();
        Given given = method.getAnnotation(Given.class);
        if (given != null) return given.value();
        Then then = method.getAnnotation(Then.class);
        if (then != null) return then.value();
        return null;
    }

    private static Object[] buildArgs(Class<?>[] types, Matcher matcher) {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            String group = matcher.group(i + 1);
            args[i] = (types[i] == int.class || types[i] == Integer.class)
                    ? Integer.parseInt(group)
                    : group;
        }
        return args;
    }
}
