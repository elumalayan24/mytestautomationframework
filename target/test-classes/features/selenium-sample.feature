Feature: Selenium Web Automation Sample

  @selenium
  Scenario: Search for a product on an e-commerce website
    Given I open the selenium browser and navigate to "https://www.google.com"
    When I search for "laptop" in the selenium search box
    And I click on the first selenium search result
    Then I should see selenium product details displayed
    And I take a selenium screenshot of the product page

  @selenium
  Scenario: Verify page title and URL
    Given I open the selenium browser and navigate to "https://www.google.com"
    When I wait for the selenium page to load completely
    Then the selenium page title should contain "Google"
    And the current selenium URL should contain "google.com"

  @selenium
  Scenario: Login functionality test
    Given I open the selenium browser and navigate to "https://www.saucedemo.com"
    When I enter selenium username "standard_user" and password "secret_sauce"
    And I click on the selenium login button
    Then I should be redirected to the selenium inventory page
    And I should see the selenium product listing

  @selenium
  Scenario: Form validation test
    Given I open the selenium browser and navigate to "https://www.saucedemo.com"
    When I enter selenium username "" and password ""
    And I click on the selenium login button
    Then I should see a selenium error message
    And I take a selenium screenshot of the error
