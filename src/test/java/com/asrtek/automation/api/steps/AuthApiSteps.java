package com.asrtek.automation.api.steps;

import com.asrtek.automation.api.context.ApiContext;
import com.asrtek.automation.api.hooks.ApiHooks;
import com.asrtek.automation.models.UserModel;
import com.asrtek.automation.utils.JsonUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.Assert;

/**
 * Step definitions for authentication API scenarios.
 *
 * After a successful login step, the JWT token is stored in ApiContext
 * and automatically injected into all subsequent authenticated requests
 * by ApiClient.authBase().
 */
public class AuthApiSteps {

    // ── Data-driven login from Users.json ─────────────────────────────────────

    @Given("I authenticate as {string} user via API")
    public void iAuthenticateAsUserViaApi(String userType) {
        UserModel user = JsonUtils.findUserByType(userType);
        Assert.assertNotNull(
                "No user with type '" + userType + "' found in Users.json", user);

        String body = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                user.getUserName(), user.getPass());

        Response response = ApiHooks.getClient().post("/auth/login", body);

        Assert.assertEquals(
                "Login failed for user type '" + userType + "'.\n"
                        + "Response: " + response.asPrettyString(),
                200, response.statusCode());

        String token = response.jsonPath().getString("token");
        Assert.assertNotNull("Login succeeded but response had no 'token' field", token);
        ApiContext.setToken(token);

        System.out.println("[AuthApiSteps] Authenticated as " + userType
                + " (" + user.getUserName() + ")");
    }

    // ── Direct credential login ────────────────────────────────────────────────

    @When("I send a login request with username {string} and password {string}")
    public void iSendALoginRequest(String username, String password) {
        String body = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        ApiHooks.getClient().post("/auth/login", body);
    }

    // ── Token state ────────────────────────────────────────────────────────────

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        ApiContext.setToken(null);
    }
}
