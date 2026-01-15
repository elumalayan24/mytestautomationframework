@playwright
Feature: Login Functionality with Playwright
  As a user
  I want to be able to log into the application using Playwright
  So that I can access my account

  Background:
    Given I am on the login page using Playwright

  Scenario: Successful login with valid credentials using Playwright
    When I enter username "standard_user" and password "secret_sauce" using Playwright
    And I click the login button using Playwright
    Then I should be logged in successfully using Playwright
    And I should see the products page using Playwright

  Scenario: Failed login with invalid credentials using Playwright
    When I enter username "invalid_user" and password "wrong_password" using Playwright
    And I click the login button using Playwright
    Then I should see an error message "Epic sadface: Username and password do not match any user in this service" using Playwright

  Scenario: Failed login with locked user using Playwright
    When I enter username "locked_out_user" and password "secret_sauce" using Playwright
    And I click the login button using Playwright
    Then I should see an error message "Epic sadface: Sorry, this user has been locked out." using Playwright
