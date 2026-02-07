Feature: Playwright Web Automation Sample

  @playwright
  Scenario: Search for a product on an e-commerce website
    Given I open the browser and navigate to "https://www.google.com"
    When I search for "laptop" in the search box
    And I click on the first search result
    Then I should see product details displayed
    And I take a screenshot of the product page

  @playwright
  Scenario: Verify page title and URL
    Given I open the browser and navigate to "https://www.google.com"
    When I wait for the page to load completely
    Then the page title should contain "Example"
    And the current URL should be "https://www.google.com"