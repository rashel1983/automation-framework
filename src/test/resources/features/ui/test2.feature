@librarytest
Feature: ASR-TEK - App-user - Add employee test

  Scenario: Admin searches for a student by name
    When @addemployee
    When I click on "Employees" option in side menu
    When I wait for  "3" seconds to load the data
    When I click on "Login History" option in side menu
    When I wait for  "3" seconds to load the data
