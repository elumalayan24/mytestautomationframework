@login
Feature: Smoke Login Test
  As a user
  I want to verify smoke login functionality
  So that I can ensure basic login works

  Background:
    Given I am on the login page

  @smoke
  Scenario: Successful login with valid credentials
    When I enter username "standard_user" and password "secret_sauce"
    And I click the login button
    Then I should be logged in successfully
    And I should see the products page
