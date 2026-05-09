@addemployee
Feature: ASR-TEK - App-user - Add employee test

  @addemployee
  Scenario: Admin searches for a student by name
    Given I am logged in as an "Test User" User
    When I click on "Employees" option in side menu
    When I click on "+ Add Employee" button
    When I enter "John" into "First Name" field
    When I enter "John Last" into "Last Name" field
    When I enter "john@email.com" into "Email" field
    When I enter "7184065000" into "Phone Number" field
    When I enter "04/24/2026" into "Hire Date" field
    When I enter "5000" into "Salary" field
    When I select "Finance" from "Department" dropdown
    When I select "Accounting Manager" from "Job" dropdown
    When I click on "Save" button