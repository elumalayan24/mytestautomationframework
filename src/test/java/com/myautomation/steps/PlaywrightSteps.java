package com.myautomation.steps;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.myautomation.sessions.PlaywrightCucumberSession;
import com.myautomation.sessions.PlaywrightSessionHolder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Paths;

public class PlaywrightSteps {

    private PlaywrightCucumberSession getSession() {
        PlaywrightCucumberSession session = PlaywrightSessionHolder.getSession();
        if (session == null) {
            throw new IllegalStateException("Playwright session not initialized. Make sure @playwright tag is used.");
        }
        return session;
    }

    @Given("I open the browser and navigate to {string}")
    public void iOpenTheBrowserAndNavigateTo(String url) {
        getSession().navigateTo(url);
    }

    @When("I search for {string} in the search box")
    public void iSearchForInTheSearchBox(String searchTerm) {
        Page page = getSession().getPage();
        try {
            // For Google, use the specific search input
            Locator searchBox = page.locator("textarea[name='q'], input[name='q'], input[type='search']").first();
            if (searchBox.count() > 0) {
                searchBox.fill(searchTerm);
                searchBox.press("Enter");
                System.out.println("✅ Search term '" + searchTerm + "' entered successfully");
            } else {
                // Fallback: try to find any visible input element
                Locator allInputs = page.locator("input:visible").first();
                if (allInputs.count() > 0) {
                    allInputs.fill(searchTerm);
                    allInputs.press("Enter");
                    System.out.println("✅ Search term '" + searchTerm + "' entered using fallback selector");
                } else {
                    throw new RuntimeException("No search input found on the page");
                }
            }
            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception e) {
            System.err.println("❌ Failed to enter search term: " + e.getMessage());
            throw e;
        }
    }

    @And("I click on the first search result")
    public void iClickOnTheFirstSearchResult() {
        Page page = getSession().getPage();
        // Try to find search results by common selectors
        Locator searchResults = page.locator(".search-result, .product-item, .item, a[href*='product']").first();
        if (searchResults.count() > 0) {
            searchResults.click();
        } else {
            // Fallback: click on first link
            page.locator("a").first().click();
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Then("I should see product details displayed")
    public void iShouldSeeProductDetailsDisplayed() {
        Page page = getSession().getPage();
        // Check for common product detail elements
        Locator productTitle = page.locator("h1, .product-title, .title, .product-name").first();
        Locator productPrice = page.locator(".price, .product-price, [data-price]").first();
        
        Assertions.assertTrue(productTitle.count() > 0, "Product title should be displayed");
        System.out.println("Product found: " + productTitle.textContent());
        
        if (productPrice.count() > 0) {
            System.out.println("Price: " + productPrice.textContent());
        }
    }

    @And("I take a screenshot of the product page")
    public void iTakeAScreenshotOfTheProductPage() {
        getSession().takeScreenshot("playwright-product");
    }

    @When("I wait for the page to load completely")
    public void iWaitForThePageToLoadCompletely() {
        getSession().waitForPageLoad();
    }

    @Then("the page title should contain {string}")
    public void thePageTitleShouldContain(String expectedTitle) {
        Page page = getSession().getPage();
        String actualTitle = page.title();
        Assertions.assertTrue(actualTitle.contains(expectedTitle),
                "Page title should contain '" + expectedTitle + "'. Actual title: " + actualTitle);
        System.out.println("Page title verified: " + actualTitle);
    }

    @And("the current URL should be {string}")
    public void theCurrentURLShouldBe(String expectedUrl) {
        Page page = getSession().getPage();
        String actualUrl = page.url();
        Assertions.assertEquals(expectedUrl, actualUrl,
                "Expected URL: " + expectedUrl + ", Actual URL: " + actualUrl);
        System.out.println("URL verified: " + actualUrl);
    }

    /**
     * Cleanup method to be called after all tests
     */
    public static void cleanupAll() {
        PlaywrightCucumberSession.cleanupAll();
    }
}
