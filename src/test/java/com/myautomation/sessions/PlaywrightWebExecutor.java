package com.myautomation.sessions;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Paths;

/**
 * Playwright Web Executor class
 * Contains all web interaction methods like navigation, screenshots, waits, etc.
 * This class handles the actual browser operations while session management is handled by session classes
 */
public class PlaywrightWebExecutor {

    private final Page page;
    private final String screenshotDir;

    public PlaywrightWebExecutor(Page page, String screenshotDir) {
        this.page = page;
        this.screenshotDir = screenshotDir;
    }

    /**
     * Get current page instance
     */
    public Page getPage() {
        return page;
    }

    /**
     * Take a screenshot with automatic naming
     */
    public void takeScreenshot(String screenshotName) {
        takeScreenshot(screenshotName, false);
    }

    /**
     * Take a screenshot with scenario name and timestamp
     */
    public void takeScreenshotForScenario(String scenarioName) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "playwright_" + scenarioName + "_" + timestamp;
        takeScreenshot(fileName, true);
    }

    /**
     * Take a screenshot with automatic naming
     */
    private void takeScreenshot(String screenshotName, boolean isScenarioScreenshot) {
        try {
            String fileName = String.format("test-output/screenshots/%s.png", screenshotName);
            
            if (page != null) {
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get(fileName))
                        .setFullPage(true));
                System.out.println("📸 Screenshot saved: " + fileName);
                if (isScenarioScreenshot) {
                    System.out.println("📸 Scenario screenshot for report: " + screenshotName);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    /**
     * Wait for page to be fully loaded
     */
    public void waitForPageLoad() {
        if (page != null) {
            page.waitForLoadState(LoadState.NETWORKIDLE);
        }
    }

    /**
     * Navigate to URL with automatic wait
     */
    public void navigateTo(String url) {
        if (page != null) {
            page.navigate(url);
            waitForPageLoad();
            System.out.println("🌐 Navigated to: " + url);
        }
    }

    
    /**
     * Click on an element
     */
    public void click(String selector) {
        if (page != null) {
            page.locator(selector).click();
            System.out.println("🖱️ Clicked on: " + selector);
        }
    }

    /**
     * Type text into an input field
     */
    public void type(String selector, String text) {
        if (page != null) {
            page.locator(selector).fill(text);
            System.out.println("⌨️ Typed text in: " + selector);
        }
    }

    /**
     * Get text from an element
     */
    public String getText(String selector) {
        if (page != null) {
            return page.locator(selector).textContent();
        }
        return null;
    }

    /**
     * Wait for element to be visible
     */
    public void waitForElement(String selector) {
        if (page != null) {
            page.locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            System.out.println("⏳ Element visible: " + selector);
        }
    }

    /**
     * Check if element is visible
     */
    public boolean isElementVisible(String selector) {
        if (page != null) {
            return page.locator(selector).isVisible();
        }
        return false;
    }

    /**
     * Scroll to element
     */
    public void scrollToElement(String selector) {
        if (page != null) {
            page.locator(selector).scrollIntoViewIfNeeded();
            System.out.println("📜 Scrolled to: " + selector);
        }
    }
}
