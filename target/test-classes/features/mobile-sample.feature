Feature: Mobile App Automation Sample

  @mobile
  Scenario: Mobile app login test
    Given I launch the mobile app
    When I tap on element with id "login_button"
    And I enter text "testuser" in field with id "username_field"
    And I enter text "password123" in field with id "password_field"
    And I tap on element with id "submit_button"
    Then I should see element with id "welcome_message"
    And I take a mobile screenshot

  @mobile
  Scenario: Mobile app navigation test
    Given I launch the mobile app
    When I tap on element with accessibility id "menu_button"
    And I tap on element with text "Settings"
    Then I should see text "Settings" on the screen
    And I should see element with id "settings_title"

  @mobile
  Scenario: Mobile app scroll test
    Given I launch the mobile app
    When I scroll down until I see text "Advanced Options"
    And I tap on element with text "Advanced Options"
    Then I should see element with id "advanced_settings"
    And I take a mobile screenshot

  @mobile
  Scenario: Mobile app swipe test
    Given I launch the mobile app
    When I swipe "left"
    And I wait for 2 seconds
    And I swipe "right"
    Then I should see element with id "home_screen"
    And I take a mobile screenshot

  @mobile
  Scenario: Mobile app form validation test
    Given I launch the mobile app
    When I tap on element with id "registration_button"
    And I enter text "" in field with id "email_field"
    And I tap on element with id "submit_button"
    Then the element with id "submit_button" should be disabled
    And I should see text "Email is required" on the screen
    And I take a mobile screenshot
