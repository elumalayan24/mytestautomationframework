package com.myautomation.steps;

import com.myautomation.core.drivers.DriverFactory;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class MobileSteps {
    
    private AppiumDriver getMobileDriver() {
        AppiumDriver driver = (AppiumDriver) DriverFactory.getDriver();
        if (driver == null) {
            throw new IllegalStateException("Mobile driver not initialized. Make sure @mobile tag is used.");
        }
        return driver;
    }
    
    private WebDriverWait getWait() {
        return new WebDriverWait(getMobileDriver(), Duration.ofSeconds(10));
    }
    
    @Given("I launch the mobile app")
    public void iLaunchTheMobileApp() {
        AppiumDriver driver = getMobileDriver();
        System.out.println("Mobile app launched successfully on " + driver.getCapabilities().getCapability("platformName"));
    }
    
    @When("I tap on element with id {string}")
    public void iTapOnElementWithId(String elementId) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(By.id(elementId)));
        element.click();
        System.out.println("Tapped on element with id: " + elementId);
    }
    
    @When("I tap on element with xpath {string}")
    public void iTapOnElementWithXpath(String xpath) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        element.click();
        System.out.println("Tapped on element with xpath: " + xpath);
    }
    
    @When("I tap on element with accessibility id {string}")
    public void iTapOnElementWithAccessibilityId(String accessibilityId) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(MobileBy.AccessibilityId(accessibilityId)));
        element.click();
        System.out.println("Tapped on element with accessibility id: " + accessibilityId);
    }
    
    @When("I enter text {string} in field with id {string}")
    public void iEnterTextInFieldWithId(String text, String fieldId) {
        AppiumDriver driver = getMobileDriver();
        WebElement field = getWait().until(ExpectedConditions.presenceOfElementLocated(By.id(fieldId)));
        field.clear();
        field.sendKeys(text);
        System.out.println("Entered text '" + text + "' in field with id: " + fieldId);
    }
    
    @When("I enter text {string} in field with xpath {string}")
    public void iEnterTextInFieldWithXpath(String text, String xpath) {
        AppiumDriver driver = getMobileDriver();
        WebElement field = getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        field.clear();
        field.sendKeys(text);
        System.out.println("Entered text '" + text + "' in field with xpath: " + xpath);
    }
    
    @When("I swipe {string}")
    public void iSwipe(String direction) {
        AppiumDriver driver = getMobileDriver();
        Dimension size = driver.manage().window().getSize();
        
        int startX, endX, startY, endY;
        
        switch (direction.toLowerCase()) {
            case "up":
                startX = size.width / 2;
                startY = (int) (size.height * 0.8);
                endX = size.width / 2;
                endY = (int) (size.height * 0.2);
                break;
            case "down":
                startX = size.width / 2;
                startY = (int) (size.height * 0.2);
                endX = size.width / 2;
                endY = (int) (size.height * 0.8);
                break;
            case "left":
                startX = (int) (size.width * 0.8);
                startY = size.height / 2;
                endX = (int) (size.width * 0.2);
                endY = size.height / 2;
                break;
            case "right":
                startX = (int) (size.width * 0.2);
                startY = size.height / 2;
                endX = (int) (size.width * 0.8);
                endY = size.height / 2;
                break;
            default:
                throw new IllegalArgumentException("Invalid swipe direction: " + direction);
        }
        
        // Perform swipe using TouchAction
        new TouchAction((PerformsTouchActions) driver)
            .press(PointOption.point(startX, startY))
            .waitAction()
            .moveTo(PointOption.point(endX, endY))
            .release()
            .perform();
        
        System.out.println("Swiped " + direction);
    }
    
    @When("I scroll down until I see text {string}")
    public void iScrollDownUntilIText(String text) {
        AppiumDriver driver = getMobileDriver();
        WebDriverWait wait = getWait();
        
        // Scroll until element is visible
        driver.findElement(MobileBy.AndroidUIAutomator(
            "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"" + text + "\"))"));
        
        // Wait for element to be visible
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@text,'" + text + "')]")));
        System.out.println("Scrolled down until text was visible: " + text);
    }
    
    @Then("I should see element with id {string}")
    public void iShouldSeeElementWithId(String elementId) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(By.id(elementId)));
        Assertions.assertTrue(element.isDisplayed(), "Element with id '" + elementId + "' should be visible");
        System.out.println("Element with id '" + elementId + "' is visible");
    }
    
    @Then("I should see element with text {string}")
    public void iShouldSeeElementWithText(String text) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@text,'" + text + "')]")));
        Assertions.assertTrue(element.isDisplayed(), "Element with text '" + text + "' should be visible");
        System.out.println("Element with text '" + text + "' is visible");
    }
    
    @Then("I should see text {string} on the screen")
    public void iShouldSeeTextOnTheScreen(String text) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@text,'" + text + "')]")));
        Assertions.assertNotNull(element, "Text '" + text + "' should be present on screen");
        System.out.println("Text '" + text + "' is present on screen");
    }
    
    @And("I wait for {int} seconds")
    public void iWaitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
            System.out.println("Waited for " + seconds + " seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Wait interrupted: " + e.getMessage());
        }
    }
    
    @And("I take a mobile screenshot")
    public void iTakeAMobileScreenshot() {
        AppiumDriver driver = getMobileDriver();
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String screenshotName = "mobile_screenshot_" + timestamp;
        
        try {
            // Take screenshot using Appium
            byte[] screenshot = driver.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            java.io.File screenshotFile = new java.io.File("test-output/screenshots/" + screenshotName + ".png");
            screenshotFile.getParentFile().mkdirs();
            java.nio.file.Files.write(screenshotFile.toPath(), screenshot);
            
            System.out.println("Mobile screenshot saved: " + screenshotFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to take mobile screenshot: " + e.getMessage());
        }
    }
    
    @Then("the element with id {string} should be enabled")
    public void theElementWithIdShouldBeEnabled(String elementId) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(By.id(elementId)));
        Assertions.assertTrue(element.isEnabled(), "Element with id '" + elementId + "' should be enabled");
        System.out.println("Element with id '" + elementId + "' is enabled");
    }
    
    @Then("the element with id {string} should be disabled")
    public void theElementWithIdShouldBeDisabled(String elementId) {
        AppiumDriver driver = getMobileDriver();
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(By.id(elementId)));
        Assertions.assertFalse(element.isEnabled(), "Element with id '" + elementId + "' should be disabled");
        System.out.println("Element with id '" + elementId + "' is disabled");
    }
}
