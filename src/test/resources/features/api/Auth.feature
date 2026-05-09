@api @auth
Feature: Authentication API

  Background:
    Given the API is running

  @smoke
  Scenario: Admin logs in with valid credentials
    When I authenticate as "Admin" user via API
    Then the API response status should be 200
    And the API response should contain field "token"
    And the API response should contain field "userType" with value "admin"
    And the API response should contain field "firstName"

  @smoke
  Scenario: App user logs in with valid credentials
    When I authenticate as "Test User" user via API
    Then the API response status should be 200
    And the API response should contain field "token"

  Scenario: Login fails with wrong password
    When I send a login request with username "admin@asr-tek.com" and password "wrongpass"
    Then the API response status should be 401
    And the API response should contain field "message"

  Scenario: Login fails with unknown username
    When I send a login request with username "nobody@unknown.com" and password "pass1"
    Then the API response status should be 401
    And the API response should contain field "message"

  Scenario: Login fails when username is missing
    When I send a login request with username "" and password "pass1"
    Then the API response status should be 400

  Scenario: Login fails when password is missing
    When I send a login request with username "admin@asr-tek.com" and password ""
    Then the API response status should be 400
