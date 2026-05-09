@api @employees
Feature: Employees Management API

  Background:
    Given the API is running
    And I authenticate as "Test User" user via API

  @smoke
  Scenario: App user retrieves all employees
    When I request the list of all employees
    Then the API response status should be 200

  Scenario: App user retrieves employee view list
    When I request the employee view list
    Then the API response status should be 200

  Scenario: App user retrieves employee joined data
    When I request employee joined data
    Then the API response status should be 200

  Scenario: App user retrieves their own profile
    When I request my user profile
    Then the API response status should be 200

  Scenario: App user can search for an employee
    When I search for employee "engineer"
    Then the API response status should be 200
