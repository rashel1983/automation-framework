@mobile @students
Feature: Mobile Students Management

  Background:
    Given I am logged in as an "Admin" User on mobile
    And I should be on the mobile "Dashboard" page

  @smoke
  Scenario: Admin navigates to Students page on mobile
    When I tap on "Students Card"
    Then I should be on the mobile "Students" page
    And I should see "Students List" on mobile

  Scenario: Admin scrolls through student list on mobile
    When I tap on "Students Card"
    And I swipe up
    And I swipe up
    Then I should be on the mobile "Students" page

  Scenario: Admin searches for a student on mobile
    When I tap on "Students Card"
    And I tap on "Search Field"
    And I enter "batch" into "Search Field" field on mobile
    Then I should be on the mobile "Students" page

  Scenario: Admin navigates to Add Student on mobile
    When I tap on "Students Card"
    And I scroll to "Add Student Button"
    And I tap on "Add Student Button"
    Then I should be on the mobile "StudentAdd" page
