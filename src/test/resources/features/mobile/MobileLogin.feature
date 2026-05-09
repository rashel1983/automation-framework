@mobile @auth
Feature: Mobile Login

  Background:
    Given I navigate to the mobile login page

  @smoke
  Scenario: Admin logs in successfully on mobile
    When I enter "admin@asr-tek.com" into "Username Field" field on mobile
    And I enter "pass1" into "Password Field" field on mobile
    And I tap on "Sign In"
    Then I should be on the mobile "Dashboard" page
    And I should see "Welcome Message" on mobile

  @smoke
  Scenario: App user logs in successfully on mobile
    When I enter "test@asr-tek.com" into "Username Field" field on mobile
    And I enter "pass1" into "Password Field" field on mobile
    And I tap on "Sign In"
    Then I should be on the mobile "Dashboard" page

  Scenario: Login fails with wrong password on mobile
    When I enter "admin@asr-tek.com" into "Username Field" field on mobile
    And I enter "wrongpass" into "Password Field" field on mobile
    And I tap on "Sign In"
    Then I should see "Error Message" on mobile

  Scenario: Shortcut login using user type
    Given I am logged in as an "Admin" User on mobile
    Then I should be on the mobile "Dashboard" page
