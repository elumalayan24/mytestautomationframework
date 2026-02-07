package com.myautomation.steps;

import com.myautomation.core.drivers.DriverFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SeleniumSteps {

    private WebDriver getDriver() {
        WebDriver driver = (WebDriver) DriverFactory.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized. Make sure @selenium tag is used.");
        }
        return driver;
    }

    private WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(10));
    }

    @Given("I open the selenium browser and navigate to {string}")
    public void iOpenTheBrowserAndNavigateTo(String url) {
        WebDriver driver = getDriver();
        driver.get(url);
        driver.manage().window().maximize();
    }

    @When("I search for {string} in the selenium search box")
    public void iSearchForInTheSearchBox(String searchTerm) {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        // Try to find search box by common attributes
        WebElement searchBox = null;
        String[] searchSelectors = {
            "input[placeholder*='search']",
            "input[name*='search']",
            "#search",
            ".search-input",
            "input[type='search']",
            "input[name='q']"
        };
        
        for (String selector : searchSelectors) {
            try {
                searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                break;
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        
        if (searchBox == null) {
            // Fallback: try to find any input element
            searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));
        }
        
        searchBox.clear();
        searchBox.sendKeys(searchTerm);
        searchBox.submit();
    }

    @And("I click on the first selenium search result")
    public void iClickOnTheFirstSearchResult() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        // Try to find search results by common selectors
        WebElement firstResult = null;
        String[] resultSelectors = {
            ".search-result",
            ".product-item",
            ".item",
            "a[href*='product']",
            ".g a", // Google search results
            "h3 a", // Common heading links
            "a"
        };
        
        for (String selector : resultSelectors) {
            try {
                firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                break;
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        
        if (firstResult != null) {
            firstResult.click();
        } else {
            throw new RuntimeException("Could not find any clickable search result");
        }
    }

    @Then("I should see selenium product details displayed")
    public void iShouldSeeProductDetailsDisplayed() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        // Check for common product detail elements
        WebElement productTitle = null;
        String[] titleSelectors = {
            "h1",
            ".product-title",
            ".title",
            ".product-name",
            ".product-name h1"
        };
        
        for (String selector : titleSelectors) {
            try {
                productTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                break;
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        
        Assertions.assertNotNull(productTitle, "Product title should be displayed");
        System.out.println("Product found: " + productTitle.getText());
        
        // Check for price if available
        try {
            WebElement productPrice = driver.findElement(By.cssSelector(".price, .product-price, [data-price]"));
            System.out.println("Price: " + productPrice.getText());
        } catch (Exception e) {
            System.out.println("Price information not found");
        }
    }

    @And("I take a selenium screenshot of the product page")
    public void iTakeAScreenshotOfTheProductPage() {
        // Screenshot is automatically handled by ReportingHooks for Selenium scenarios
        System.out.println("Screenshot will be taken automatically by ReportingHooks");
    }

    @When("I wait for the selenium page to load completely")
    public void iWaitForThePageToLoadCompletely() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
    }

    @Then("the selenium page title should contain {string}")
    public void thePageTitleShouldContain(String expectedTitle) {
        WebDriver driver = getDriver();
        String actualTitle = driver.getTitle();
        Assertions.assertTrue(actualTitle.contains(expectedTitle),
                "Page title should contain '" + expectedTitle + "'. Actual title: " + actualTitle);
        System.out.println("Page title verified: " + actualTitle);
    }

    @And("the current selenium URL should contain {string}")
    public void theCurrentURLShouldContain(String expectedUrl) {
        WebDriver driver = getDriver();
        String actualUrl = driver.getCurrentUrl();
        Assertions.assertTrue(actualUrl.contains(expectedUrl),
                "URL should contain '" + expectedUrl + "'. Actual URL: " + actualUrl);
        System.out.println("URL verified: " + actualUrl);
    }

    @And("the current selenium URL should be {string}")
    public void theCurrentURLShouldBe(String expectedUrl) {
        WebDriver driver = getDriver();
        String actualUrl = driver.getCurrentUrl();
        Assertions.assertEquals(expectedUrl, actualUrl,
                "Expected URL: " + expectedUrl + ", Actual URL: " + actualUrl);
        System.out.println("URL verified: " + actualUrl);
    }

    @When("I enter selenium username {string} and password {string}")
    public void iEnterUsernameAndPassword(String username, String password) {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        
        usernameField.clear();
        usernameField.sendKeys(username);
        
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    @And("I click on the selenium login button")
    public void iClickOnTheLoginButton() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginButton.click();
    }

    @Then("I should be redirected to the selenium inventory page")
    public void iShouldBeRedirectedToTheInventoryPage() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        // Wait for inventory page to load
        wait.until(ExpectedConditions.urlContains("inventory"));
        
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("inventory"),
                "Should be redirected to inventory page. Current URL: " + currentUrl);
    }

    @And("I should see the selenium product listing")
    public void iShouldSeeTheProductListing() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        // Check for inventory items
        WebElement inventoryContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("inventory_list")));
        
        // Check if there are products displayed
        java.util.List<WebElement> products = driver.findElements(By.className("inventory_item"));
        Assertions.assertTrue(products.size() > 0, "Products should be displayed on the page");
        
        System.out.println("Found " + products.size() + " products in the inventory");
    }

    @Then("I should see a selenium error message")
    public void iShouldSeeAnErrorMessage() {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message-container, .error")));
        
        String errorText = errorMessage.getText();
        Assertions.assertNotNull(errorText, "Error message should be displayed");
        Assertions.assertFalse(errorText.isEmpty(), "Error message should not be empty");
        
        System.out.println("Error message displayed: " + errorText);
    }

    @And("I take a selenium screenshot of the error")
    public void iTakeAScreenshotOfTheError() {
        // Screenshot is automatically handled by ReportingHooks for Selenium scenarios
        System.out.println("Error screenshot will be taken automatically by ReportingHooks");
    }
}
