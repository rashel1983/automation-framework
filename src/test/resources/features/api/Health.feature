@api @health
Feature: Application Health API

  @smoke
  Scenario: Health endpoint returns UP status
    When I check the application health
    Then the API response status should be 200
    And the API response should contain field "status" with value "UP"
