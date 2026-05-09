@api @students
Feature: Students Management API

  Background:
    Given the API is running
    And I authenticate as "Admin" user via API

  @smoke
  Scenario: Admin retrieves all students
    When I request the list of all students
    Then the API response status should be 200

  @smoke
  Scenario: Admin retrieves approved student accounts
    When I request the list of approved students
    Then the API response status should be 200

  Scenario: Admin retrieves pending student accounts
    When I request the list of pending students
    Then the API response status should be 200

  Scenario: Admin retrieves rejected student accounts
    When I request the list of rejected students
    Then the API response status should be 200

  Scenario: Admin can search for a student by keyword
    When I search for student "batch"
    Then the API response status should be 200

  Scenario: Admin accesses dashboard summary
    When I request the admin dashboard
    Then the API response status should be 200

  Scenario: Admin retrieves user activity report
    When I request the user activity report
    Then the API response status should be 200

  Scenario: Admin retrieves visitor activity report
    When I request the visitor activity report
    Then the API response status should be 200
