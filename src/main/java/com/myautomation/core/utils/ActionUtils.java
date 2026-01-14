package com.myautomation.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class providing common web automation actions with built-in exception handling
 * for both Selenium and Playwright.
 */
public final class ActionUtils {
    private static final Logger logger = LoggerFactory.getLogger(ActionUtils.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLLING_INTERVAL = Duration.ofMillis(500);
    private static final String SCREENSHOTS_DIR = "test-output/screenshots";

    private ActionUtils() {
        // Private constructor to prevent instantiation
    }

    // ==================== SELENIUM ACTIONS ====================

    /**
     * Find an element with retry and explicit wait
     */
    public static WebElement findElement(WebDriver driver, By locator) {
        return AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT, POLLING_INTERVAL);
                return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            },
            "finding element: " + locator
        );
    }

    /**
     * Click an element with retry and explicit wait
     */
    public static void click(WebDriver driver, By locator) {
        AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                WebElement element = findElement(driver, locator);
                WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT, POLLING_INTERVAL);
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                return null;
            },
            "clicking element: " + locator
        );
    }

    /**
     * Type text into an input field
     */
    public static void type(WebDriver driver, By locator, String text) {
        AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                WebElement element = findElement(driver, locator);
                element.clear();
                element.sendKeys(text);
                return null;
            },
            "typing text: '" + text + "' into: " + locator
        );
    }

    /**
     * Select an option by visible text from a dropdown
     */
    public static void selectByVisibleText(WebDriver driver, By locator, String text) {
        AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                Select select = new Select(findElement(driver, locator));
                select.selectByVisibleText(text);
                return null;
            },
            "selecting by visible text: '" + text + "' from: " + locator
        );
    }

    /**
     * Hover over an element
     */
    public static void hover(WebDriver driver, By locator) {
        AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                new Actions(driver)
                    .moveToElement(findElement(driver, locator))
                    .perform();
                return null;
            },
            "hovering over element: " + locator
        );
    }

    /**
     * Get text from an element
     */
    public static String getText(WebDriver driver, By locator) {
        return AutomationExceptionHandler.executeSeleniumAction(
            () -> findElement(driver, locator).getText(),
            "getting text from: " + locator
        );
    }

    // ==================== PLAYWRIGHT ACTIONS ====================

    /**
     * Find a Playwright element with retry
     */
    public static ElementHandle findElement(Page page, String selector) {
        return AutomationExceptionHandler.executePlaywrightAction(
            () -> page.waitForSelector(selector, 
                new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(DEFAULT_TIMEOUT.toMillis())),
            "finding element: " + selector
        );
    }

    /**
     * Click an element with retry
     */
    public static void click(Page page, String selector) {
        AutomationExceptionHandler.executePlaywrightAction(
            () -> {
                findElement(page, selector).click();
                return null;
            },
            "clicking element: " + selector
        );
    }

    /**
     * Type text into an input field
     */
    public static void type(Page page, String selector, String text) {
        AutomationExceptionHandler.executePlaywrightAction(
            () -> {
                ElementHandle element = findElement(page, selector);
                element.click();
                element.fill("");
                element.type(text);
                return null;
            },
            "typing text: '" + text + "' into: " + selector
        );
    }

    /**
     * Select an option by visible text from a dropdown
     */
    public static void selectByVisibleText(Page page, String selector, String text) {
        AutomationExceptionHandler.executePlaywrightAction(
            () -> {
                page.selectOption(selector, new SelectOption().setLabel(text));
                return null;
            },
            "selecting by visible text: '" + text + "' from: " + selector
        );
    }

    /**
     * Hover over an element
     */
    public static void hover(Page page, String selector) {
        AutomationExceptionHandler.executePlaywrightAction(
            () -> {
                findElement(page, selector).hover();
                return null;
            },
            "hovering over element: " + selector
        );
    }

    /**
     * Get text from an element
     */
    public static String getText(Page page, String selector) {
        return AutomationExceptionHandler.executePlaywrightAction(
            () -> findElement(page, selector).textContent().trim(),
            "getting text from: " + selector
        );
    }

    /**
     * Wait for page to load completely
     */
    public static void waitForPageLoad(Page page) {
        AutomationExceptionHandler.executePlaywrightAction(
            () -> {
                page.waitForLoadState(LoadState.LOAD);
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                page.waitForLoadState(LoadState.NETWORKIDLE);
                return null;
            },
            "waiting for page to load"
        );
    }

    // ==================== COMMON UTILITIES ====================

    /**
     * Take a screenshot and save it to the screenshots directory
     * @param driver WebDriver instance
     * @param testName Name of the test for the screenshot file
     * @return Path to the saved screenshot file, or null if failed
     */
    /**
     * Take a screenshot and save it to the screenshots directory
     * @param driver WebDriver instance
     * @param testName Name of the test for the screenshot file
     * @return Path to the saved screenshot file, or null if failed
     */
    public static String takeScreenshot(WebDriver driver, String testName) {
        return AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                try {
                    // Ensure screenshots directory exists
                    Path screenshotDir = Paths.get(SCREENSHOTS_DIR);
                    if (!Files.exists(screenshotDir)) {
                        try {
                            Files.createDirectories(screenshotDir);
                            logger.debug("Created screenshots directory: {}", screenshotDir.toAbsolutePath());
                        } catch (IOException e) {
                            logger.error("Failed to create screenshots directory: {}", SCREENSHOTS_DIR, e);
                            throw new RuntimeException("Failed to create screenshots directory: " + SCREENSHOTS_DIR, e);
                        }
                    }
                    
                    // Generate filename with timestamp
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                    String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_.]", "_");
                    String fileName = String.format("%s_%s.png", safeTestName, timestamp);
                    Path filePath = screenshotDir.resolve(fileName);
                    
                    // Take screenshot
                    File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    
                    // Save to file
                    try {
                        Files.copy(screenshotFile.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        logger.info("Screenshot saved to: {}", filePath.toAbsolutePath());
                        return filePath.toString();
                    } catch (IOException e) {
                        logger.error("Failed to save screenshot to: {}", filePath, e);
                        throw new RuntimeException("Failed to save screenshot to: " + filePath, e);
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error while taking screenshot", e);
                    throw e;
                }
            },
            "taking screenshot for test: " + testName
        );
    }

    /**
     * Take a screenshot using Playwright and save it to the screenshots directory
     * @param page Playwright Page instance
     * @param testName Name of the test for the screenshot file
     * @return Path to the saved screenshot file, or null if failed
     */
    public static String takeScreenshot(Page page, String testName) {
        try {
            // Ensure screenshots directory exists
            Path screenshotDir = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(screenshotDir)) {
                try {
                    Files.createDirectories(screenshotDir);
                    logger.info("Created screenshots directory at: {}", screenshotDir.toAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to create screenshots directory: {}", e.getMessage());
                    // Try to use system temp directory as fallback
                    screenshotDir = Paths.get(System.getProperty("java.io.tmpdir"), "screenshots");
                    Files.createDirectories(screenshotDir);
                    logger.warn("Using fallback screenshots directory: {}", screenshotDir.toAbsolutePath());
                }
            }
            
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_.]", "_");
            String fileName = String.format("%s_%s.png", safeTestName, timestamp);
            Path filePath = screenshotDir.resolve(fileName);
            
            // Take and save screenshot
            page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setPath(filePath)
            );
            
            logger.info("Screenshot saved to: {}", filePath.toAbsolutePath());
            return filePath.toString();
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Scroll to an element (Selenium)
     */
    public static void scrollTo(WebDriver driver, By locator) {
        AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                WebElement element = findElement(driver, locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                return null;
            },
            "scrolling to element: " + locator
        );
    }

    /**
     * Scroll to an element (Playwright)
     */
    public static void scrollTo(Page page, String selector) {
        AutomationExceptionHandler.executePlaywrightAction(
                () -> {
                    page.evaluate(
                            "selector => document.querySelector(selector).scrollIntoView()",
                            selector
                    );
                    return null;
                },
                "scrolling to element: " + selector
        );
    }


    /**
     * Wait for an element to be visible (Selenium)
     */
    public static WebElement waitForVisibility(WebDriver driver, By locator, Duration timeout) {
        return AutomationExceptionHandler.executeSeleniumAction(
            () -> {
                WebDriverWait wait = new WebDriverWait(driver, timeout != null ? timeout : DEFAULT_TIMEOUT);
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            },
            "waiting for visibility of element: " + locator
        );
    }

    /**
     * Wait for an element to be visible (Playwright)
     */
    public static ElementHandle waitForVisibility(Page page, String selector, Duration timeout) {
        return AutomationExceptionHandler.executePlaywrightAction(
            () -> page.waitForSelector(selector,
                new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout((timeout != null ? timeout : DEFAULT_TIMEOUT).toMillis())
            ),
            "waiting for visibility of element: " + selector
        );
    }
}
