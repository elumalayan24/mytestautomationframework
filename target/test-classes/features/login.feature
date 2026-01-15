
@login
Feature: Login Functionality
  As a user
  I want to be able to log into the application
  So that I can access my account

  Background:
    Given I am on the login page



  Scenario: Successful login with valid credentials
    When I enter username "standard_user" and password "secret_sauce"
    And I click the login button
    Then I should be logged in successfully


  @regressions
  Scenario: Failed login with invalid credentials
    When I enter username "invalid_user" and password "wrong_password"
    And I click the login button
    Then I should see an error message "Epic sadface: Username and password do not match any user in this service"
