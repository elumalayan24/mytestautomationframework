package com.myautomation.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.WaitForSelectorOptions;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.WaitForSelectorOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.function.Function;


import java.time.Duration;
import java.util.function.Function;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility class providing wait methods for both Playwright and Selenium.
 * This class helps in handling dynamic waits in test automation scripts.
 */
public final class WaitUtils {
    
    // Default timeout values in seconds
    private static final long DEFAULT_TIMEOUT = 30;
    private static final long DEFAULT_POLLING_INTERVAL = 500; // milliseconds
    
    private WaitUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * ==============================================
     * PLAYWRIGHT WAIT METHODS
     * ==============================================
     */
    
    /**
     * Waits for an element to be visible on the page using Playwright.
     *
     * @param page      The Playwright Page object
     * @param selector  The CSS or XPath selector of the element to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     * @return The located element
     * @throws com.microsoft.playwright.TimeoutError if the element is not visible within the timeout
     */
    public static Locator waitForElementVisible(Page page, String selector, long timeoutMs) {
        Locator locator = page.locator(selector).first();
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(timeoutMs));
        return locator;
    }
    
    /**
     * Waits for an element to be visible on the page using Playwright with default timeout.
     *
     * @param page     The Playwright Page object
     * @param selector The CSS or XPath selector of the element to wait for
     * @return The located element
     */
    public static Locator waitForElementVisible(Page page, String selector) {
        return waitForElementVisible(page, selector, DEFAULT_TIMEOUT * 1000);
    }
    
    /**
     * Waits for an element to be hidden/removed from the DOM using Playwright.
     *
     * @param page      The Playwright Page object
     * @param selector  The CSS or XPath selector of the element to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     */
    public static void waitForElementHidden(Page page, String selector, long timeoutMs) {
        page.waitForSelector(selector, 
            new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(timeoutMs));
    }
    
    /**
     * Waits for an element to be hidden/removed from the DOM using Playwright with default timeout.
     *
     * @param page     The Playwright Page object
     * @param selector The CSS or XPath selector of the element to wait for
     */
    public static void waitForElementHidden(Page page, String selector) {
        waitForElementHidden(page, selector, DEFAULT_TIMEOUT * 1000);
    }
    
    /**
     * Waits for a condition to be true within a specified timeout using Playwright.
     *
     * @param page      The Playwright Page object
     * @param condition The condition to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     * @throws com.microsoft.playwright.TimeoutError if the condition is not met within the timeout
     */
    public static void waitForCondition(Page page, Runnable condition, long timeoutMs) {
        page.waitForTimeout(100); // Small delay to prevent race conditions
        long endTime = System.currentTimeMillis() + timeoutMs;
        
        while (System.currentTimeMillis() < endTime) {
            try {
                condition.run();
                return;
            } catch (Exception e) {
                // Wait before retrying
                try {
                    Thread.sleep(DEFAULT_POLLING_INTERVAL);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Wait was interrupted", ie);
                }
            }
        }
        
        // One last try that will throw the actual exception if it fails
        condition.run();
    }
    
    /**
     * ==============================================
     * SELENIUM WAIT METHODS
     * ==============================================
     */
    
    /**
     * Creates a new WebDriverWait instance with the default timeout.
     *
     * @param driver The WebDriver instance
     * @return A new WebDriverWait instance
     */
    public static WebDriverWait createWebDriverWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }
    
    /**
     * Creates a new FluentWait instance with the default timeout and polling interval.
     *
     * @param driver The WebDriver instance
     * @return A new FluentWait instance
     */
    public static FluentWait<WebDriver> createFluentWait(WebDriver driver) {
        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
            .pollingEvery(Duration.ofMillis(DEFAULT_POLLING_INTERVAL))
            .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }
    
    /**
     * Waits for an element to be visible on the page using Selenium.
     *
     * @param driver  The WebDriver instance
     * @param by      The By selector of the element to wait for
     * @param timeout The maximum time to wait in seconds
     * @return The located WebElement
     */
    public static WebElement waitForElementVisible(WebDriver driver, By by, long timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(ExpectedConditions.visibilityOfElementLocated(by));
    }
    
    /**
     * Waits for an element to be visible on the page using Selenium with default timeout.
     *
     * @param driver The WebDriver instance
     * @param by     The By selector of the element to wait for
     * @return The located WebElement
     */
    public static WebElement waitForElementVisible(WebDriver driver, By by) {
        return waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be clickable using Selenium.
     *
     * @param driver  The WebDriver instance
     * @param by      The By selector of the element to wait for
     * @param timeout The maximum time to wait in seconds
     * @return The clickable WebElement
     */
    public static WebElement waitForElementClickable(WebDriver driver, By by, long timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(ExpectedConditions.elementToBeClickable(by));
    }
    
    /**
     * Waits for an element to be clickable using Selenium with default timeout.
     *
     * @param driver The WebDriver instance
     * @param by     The By selector of the element to wait for
     * @return The clickable WebElement
     */
    public static WebElement waitForElementClickable(WebDriver driver, By by) {
        return waitForElementClickable(driver, by, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be invisible or not present on the DOM using Selenium.
     *
     * @param driver  The WebDriver instance
     * @param by      The By selector of the element to wait for
     * @param timeout The maximum time to wait in seconds
     * @return true if the element is invisible or not present, false if the timeout expires
     */
    public static boolean waitForElementInvisible(WebDriver driver, By by, long timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(ExpectedConditions.invisibilityOfElementLocated(by));
    }
    
    /**
     * Waits for an element to be invisible or not present on the DOM using Selenium with default timeout.
     *
     * @param driver The WebDriver instance
     * @param by     The By selector of the element to wait for
     * @return true if the element is invisible or not present, false if the timeout expires
     */
    public static boolean waitForElementInvisible(WebDriver driver, By by) {
        return waitForElementInvisible(driver, by, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for a condition to be true within a specified timeout using Selenium.
     *
     * @param <T>       The return type of the condition
     * @param wait      The WebDriverWait instance to use
     * @param condition The condition to wait for
     * @return The result of the condition
     */
    public static <T> T waitForCondition(WebDriverWait wait, Function<WebDriver, T> condition) {
        return wait.until(condition);
    }
    
    /**
     * Waits for a condition to be true within a specified timeout using Selenium.
     *
     * @param <T>       The return type of the condition
     * @param driver    The WebDriver instance
     * @param condition The condition to wait for
     * @param timeout   The maximum time to wait in seconds
     * @return The result of the condition
     */
    public static <T> T waitForCondition(WebDriver driver, Function<WebDriver, T> condition, long timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(condition);
    }
    
    /**
     * Waits for a condition to be true within the default timeout using Selenium.
     *
     * @param <T>       The return type of the condition
     * @param driver    The WebDriver instance
     * @param condition The condition to wait for
     * @return The result of the condition
     */
    public static <T> T waitForCondition(WebDriver driver, Function<WebDriver, T> condition) {
        return waitForCondition(driver, condition, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for a page to load completely using Selenium.
     *
     * @param driver The WebDriver instance
     * @param timeout The maximum time to wait in seconds
     */
    public static void waitForPageLoad(WebDriver driver, long timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }
    
    /**
     * Waits for a page to load completely using Selenium with default timeout.
     *
     * @param driver The WebDriver instance
     */
    public static void waitForPageLoad(WebDriver driver) {
        waitForPageLoad(driver, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an alert to be present using Selenium.
     *
     * @param driver  The WebDriver instance
     * @param timeout The maximum time to wait in seconds
     * @return The alert that was present
     */
    public static org.openqa.selenium.Alert waitForAlert(WebDriver driver, long timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(ExpectedConditions.alertIsPresent());
    }
    
    /**
     * Waits for an alert to be present using Selenium with default timeout.
     *
     * @param driver The WebDriver instance
     * @return The alert that was present
     */
    public static org.openqa.selenium.Alert waitForAlert(WebDriver driver) {
        return waitForAlert(driver, DEFAULT_TIMEOUT);
    }
}
