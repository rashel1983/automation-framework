package com.asrtek.automation.api.context;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local storage for API test state.
 * Follows the same ThreadLocal pattern used by DriverManager and PageContext
 * so API tests are safe to run in parallel.
 *
 * Stores:
 *   - lastResponse  : the most recent REST Assured response
 *   - jwtToken      : bearer token acquired after login
 *   - savedValues   : named values extracted from responses (e.g. student id)
 */
public class ApiContext {

    private static final ThreadLocal<Response> lastResponse = new ThreadLocal<>();
    private static final ThreadLocal<String>   jwtToken     = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String>> savedValues =
            ThreadLocal.withInitial(HashMap::new);

    public static void setResponse(Response response) { lastResponse.set(response); }
    public static Response getResponse()              { return lastResponse.get(); }

    public static void setToken(String token)         { jwtToken.set(token); }
    public static String getToken()                   { return jwtToken.get(); }
    public static boolean hasToken()                  { return jwtToken.get() != null; }

    public static void save(String key, String value) { savedValues.get().put(key, value); }
    public static String getSaved(String key)         { return savedValues.get().get(key); }

    /** Called in @After hook to clean up between scenarios. */
    public static void reset() {
        lastResponse.remove();
        jwtToken.remove();
        savedValues.remove();
    }

    private ApiContext() {}
}
