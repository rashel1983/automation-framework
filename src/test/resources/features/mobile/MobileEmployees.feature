@mobile @employees
Feature: Mobile Employees Management

  Background:
    Given I am logged in as an "Test User" User on mobile
    And I should be on the mobile "Dashboard" page

  @smoke
  Scenario: App user navigates to Employees page on mobile
    When I tap on "Employees Card"
    Then I should be on the mobile "Employees" page
    And I should see "Employees List" on mobile

  Scenario: App user scrolls through employee list on mobile
    When I tap on "Employees Card"
    And I swipe up
    And I swipe down
    Then I should be on the mobile "Employees" page

  Scenario: App user searches for an employee on mobile
    When I tap on "Employees Card"
    And I tap on "Search Field"
    And I enter "engineer" into "Search Field" field on mobile
    Then I should be on the mobile "Employees" page
