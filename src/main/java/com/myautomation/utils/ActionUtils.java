package com.myautomation.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modern ActionUtils for Selenium and Playwright with fluent API.
 * Supports automatic waits, retries, and screenshots.
 */
public final class ActionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ActionUtils.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String SCREENSHOTS_DIR = "test-output/screenshots";

    private ActionUtils() { }

    // ---------------- Static Factory Methods ----------------
    public static ActionFluent with(WebDriver driver, String testName) {
        return new ActionFluent(driver, testName);
    }

    public static ActionFluent with(Page page, String testName) {
        return new ActionFluent(page, testName);
    }

    // ----------------- Fluent Wrapper -----------------
    public static class ActionFluent {
        private final WebDriver seleniumDriver;
        private final Page playwrightPage;
        private final String testName;

        public ActionFluent(WebDriver driver, String testName) {
            this.seleniumDriver = driver;
            this.playwrightPage = null;
            this.testName = testName;
        }

        public ActionFluent(Page page, String testName) {
            this.seleniumDriver = null;
            this.playwrightPage = page;
            this.testName = testName;
        }

        // ---------------- Actions ----------------

        public ActionFluent click(String locator) {
            if (seleniumDriver != null) clickSelenium(seleniumDriver, By.cssSelector(locator));
            else if (playwrightPage != null) clickPlaywright(playwrightPage, locator);
            return this;
        }

        public ActionFluent type(String locator, String text) {
            if (seleniumDriver != null) typeSelenium(seleniumDriver, By.cssSelector(locator), text);
            else if (playwrightPage != null) typePlaywright(playwrightPage, locator, text);
            return this;
        }

        public ActionFluent hover(String locator) {
            if (seleniumDriver != null) hoverSelenium(seleniumDriver, By.cssSelector(locator));
            else if (playwrightPage != null) hoverPlaywright(playwrightPage, locator);
            return this;
        }

        public String getText(String locator) {
            if (seleniumDriver != null) return getTextSelenium(seleniumDriver, By.cssSelector(locator));
            else if (playwrightPage != null) return getTextPlaywright(playwrightPage, locator);
            return null;
        }

        public ActionFluent waitForVisibility(String locator) {
            if (seleniumDriver != null) waitForVisibilitySelenium(seleniumDriver, By.cssSelector(locator), DEFAULT_TIMEOUT);
            else if (playwrightPage != null) waitForVisibilityPlaywright(playwrightPage, locator, DEFAULT_TIMEOUT);
            return this;
        }

        public ActionFluent scrollTo(String locator) {
            if (seleniumDriver != null) scrollToSelenium(seleniumDriver, By.cssSelector(locator));
            else if (playwrightPage != null) scrollToPlaywright(playwrightPage, locator);
            return this;
        }

        public void screenshot() {
            if (seleniumDriver != null) takeScreenshotSelenium(seleniumDriver, testName);
            else if (playwrightPage != null) takeScreenshotPlaywright(playwrightPage, testName);
        }

        // ---------------- Internal Selenium Methods ----------------
        private void clickSelenium(WebDriver driver, By locator) {
            WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            el.click();
        }

        private void typeSelenium(WebDriver driver, By locator, String text) {
            WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            el.clear();
            el.sendKeys(text);
        }

        private void hoverSelenium(WebDriver driver, By locator) {
            WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            new Actions(driver).moveToElement(el).perform();
        }

        private String getTextSelenium(WebDriver driver, By locator) {
            WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return el.getText();
        }

        private String getTextPlaywright(Page page, String selector) {
            ElementHandle element = page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
            return element.textContent().trim();
        }

        private void waitForVisibilitySelenium(WebDriver driver, By locator, Duration timeout) {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
        }

        private void scrollToSelenium(WebDriver driver, By locator) {
            WebElement el = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
        }

        // ---------------- Internal Playwright Methods ----------------
        private void clickPlaywright(Page page, String selector) {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
            page.click(selector);
        }

        private void typePlaywright(Page page, String selector, String text) {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
            page.fill(selector, text);
        }

        private void hoverPlaywright(Page page, String selector) {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
            page.hover(selector);
        }

        private void waitForVisibilityPlaywright(Page page, String selector, Duration timeout) {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout.toMillis()));
        }

        private void scrollToPlaywright(Page page, String selector) {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
            page.locator(selector).scrollIntoViewIfNeeded();
        }

        private void takeScreenshotSelenium(WebDriver driver, String testName) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("%s/%s_%s.png", SCREENSHOTS_DIR, testName, timestamp);
                Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), Paths.get(fileName));
                logger.info("Screenshot saved: {}", fileName);
            } catch (Exception e) {
                logger.error("Failed to take screenshot", e);
            }
        }

        private void takeScreenshotPlaywright(Page page, String testName) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("%s/%s_%s.png", SCREENSHOTS_DIR, testName, timestamp);
                Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fileName)));
                logger.info("Screenshot saved: {}", fileName);
            } catch (Exception e) {
                logger.error("Failed to take screenshot", e);
            }
        }
    }

    // ---------------- Static Utility Methods ----------------
    public static void clickSelenium(WebDriver driver, By locator) {
        WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    public static void clickPlaywright(Page page, String selector) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
        page.click(selector);
    }

    public static void typeSelenium(WebDriver driver, By locator, String text) {
        WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    public static void typePlaywright(Page page, String selector, String text) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
        page.fill(selector, text);
    }

    public static String getTextSelenium(WebDriver driver, By locator) {
        WebElement el = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        return el.getText();
    }

    public static String getTextPlaywright(Page page, String selector) {
        ElementHandle element = page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(DEFAULT_TIMEOUT.toMillis()));
        return element.textContent().trim();
    }

    public static void waitForVisibilitySelenium(WebDriver driver, By locator, Duration timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static void waitForVisibilityPlaywright(Page page, String selector, Duration timeout) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout.toMillis()));
    }

    public static void takeScreenshot(WebDriver driver, Page page, String testName) {
        if (driver != null) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("%s/%s_%s.png", SCREENSHOTS_DIR, testName, timestamp);
                Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), Paths.get(fileName));
                logger.info("Screenshot saved: {}", fileName);
            } catch (Exception e) {
                logger.error("Failed to take screenshot", e);
            }
        } else if (page != null) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("%s/%s_%s.png", SCREENSHOTS_DIR, testName, timestamp);
                Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fileName)));
                logger.info("Screenshot saved: {}", fileName);
            } catch (Exception e) {
                logger.error("Failed to take screenshot", e);
            }
        }
    }
}
