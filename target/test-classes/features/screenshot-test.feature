Feature: Screenshot Test

  @playwright
  Scenario: Test screenshot display in report
    Given I open the browser and navigate to "https://example.com"
    Then the page title should contain "Example"
