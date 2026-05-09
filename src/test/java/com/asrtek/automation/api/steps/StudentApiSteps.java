package com.asrtek.automation.api.steps;

import com.asrtek.automation.api.context.ApiContext;
import com.asrtek.automation.api.hooks.ApiHooks;
import io.cucumber.java.en.When;

/**
 * Step definitions for Student management API scenarios.
 * All requests require an ADMIN jwt token in ApiContext.
 */
public class StudentApiSteps {

    @When("I request the list of all students")
    public void iRequestAllStudents() {
        ApiHooks.getClient().get("/admin/students");
    }

    @When("I request the list of approved students")
    public void iRequestApprovedStudents() {
        ApiHooks.getClient().get("/admin/students/accounts/approved");
    }

    @When("I request the list of pending students")
    public void iRequestPendingStudents() {
        ApiHooks.getClient().get("/admin/students/accounts/pending");
    }

    @When("I request the list of rejected students")
    public void iRequestRejectedStudents() {
        ApiHooks.getClient().get("/admin/students/accounts/rejected");
    }

    @When("I search for student {string}")
    public void iSearchForStudent(String keyword) {
        ApiHooks.getClient().get("/admin/students/search?q=" + keyword);
    }

    @When("I request student with the saved id")
    public void iRequestStudentWithSavedId() {
        String id = ApiContext.getSaved("studentId");
        ApiHooks.getClient().get("/admin/students/" + id);
    }

    @When("I request the admin dashboard")
    public void iRequestAdminDashboard() {
        ApiHooks.getClient().get("/admin/dashboard");
    }

    @When("I request the user activity report")
    public void iRequestUserActivityReport() {
        ApiHooks.getClient().get("/admin/reports/users-activity");
    }

    @When("I request the visitor activity report")
    public void iRequestVisitorReport() {
        ApiHooks.getClient().get("/admin/reports/visitors");
    }
}
