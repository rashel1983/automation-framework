package com.asrtek.automation.api.client;

import com.asrtek.automation.api.context.ApiContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Thin REST Assured wrapper used by all API step definitions.
 *
 * Every method stores the response in ApiContext so step definitions
 * can inspect it without passing it around as a parameter.
 *
 * Usage:
 *   ApiClient client = new ApiClient(ConfigReader.getInstance().getApiBaseUrl());
 *   client.get("/admin/students");
 *   int status = ApiContext.getResponse().statusCode();
 */
public class ApiClient {

    public ApiClient(String baseUrl) {
        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    // ── Unauthenticated requests (login, health) ──────────────────────────────

    public Response post(String path, String jsonBody) {
        Response response = base()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(path)
                .then()
                .extract().response();
        ApiContext.setResponse(response);
        return response;
    }

    public Response getPublic(String path) {
        Response response = base()
                .when()
                .get(path)
                .then()
                .extract().response();
        ApiContext.setResponse(response);
        return response;
    }

    // ── Authenticated requests ────────────────────────────────────────────────

    public Response get(String path) {
        Response response = authBase()
                .when()
                .get(path)
                .then()
                .extract().response();
        ApiContext.setResponse(response);
        return response;
    }

    public Response postAuth(String path, String jsonBody) {
        Response response = authBase()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(path)
                .then()
                .extract().response();
        ApiContext.setResponse(response);
        return response;
    }

    public Response put(String path, String jsonBody) {
        Response response = authBase()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .put(path)
                .then()
                .extract().response();
        ApiContext.setResponse(response);
        return response;
    }

    public Response delete(String path) {
        Response response = authBase()
                .when()
                .delete(path)
                .then()
                .extract().response();
        ApiContext.setResponse(response);
        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RequestSpecification base() {
        return given()
                .accept(ContentType.JSON)
                .log().ifValidationFails();
    }

    private RequestSpecification authBase() {
        String token = ApiContext.getToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException(
                    "[ApiClient] No JWT token in ApiContext. " +
                    "Call an authentication step before making authenticated requests.");
        }
        return base().header("Authorization", "Bearer " + token);
    }
}
