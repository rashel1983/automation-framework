package com.asrtek.automation.api.steps;

import com.asrtek.automation.api.context.ApiContext;
import com.asrtek.automation.api.hooks.ApiHooks;
import io.cucumber.java.en.When;

/**
 * Step definitions for Employee management API scenarios.
 * Requires an APPUSER jwt token in ApiContext.
 */
public class EmployeeApiSteps {

    @When("I request the list of all employees")
    public void iRequestAllEmployees() {
        ApiHooks.getClient().get("/employees/all");
    }

    @When("I request employee joined data")
    public void iRequestEmployeeJoinedData() {
        ApiHooks.getClient().get("/employees/joinedData");
    }

    @When("I request the employee view list")
    public void iRequestEmployeeViewList() {
        ApiHooks.getClient().get("/viewemployee/all");
    }

    @When("I search for employee {string}")
    public void iSearchForEmployee(String keyword) {
        ApiHooks.getClient().get("/viewemployee/search?q=" + keyword);
    }

    @When("I request employee with the saved id")
    public void iRequestEmployeeWithSavedId() {
        String id = ApiContext.getSaved("employeeId");
        ApiHooks.getClient().get("/employees/employee/" + id);
    }

    @When("I request my user profile")
    public void iRequestMyProfile() {
        ApiHooks.getClient().get("/user/profile");
    }
}
