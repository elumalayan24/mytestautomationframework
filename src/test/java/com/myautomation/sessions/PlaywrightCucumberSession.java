package com.myautomation.sessions;

import com.microsoft.playwright.*;
import com.myautomation.utils.BrowserConfigUtil;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

/**
 * JUnit Playwright Session class for Cucumber tests
 * Provides automatic setup and teardown of browser, context, and page objects
 * Only contains session management methods - web operations are handled by PlaywrightWebExecutor
 */
public class PlaywrightCucumberSession {

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected PlaywrightWebExecutor executor;

    // Configuration options
    private static final boolean HEADLESS = false;
    private static final String SCREENSHOT_DIR = "test-output/screenshots/";

    /**
     * Setup method to be called from @Before in Cucumber steps
     */
    public void setUp() {
        try {
            if (playwright == null) {
                playwright = Playwright.create();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(HEADLESS)
                        .setArgs(java.util.Arrays.asList(
                            "--start-maximized", 
                            "--disable-infobars",
                            "--disable-extensions",
                            "--disable-gpu",
                            "--disable-dev-shm-usage",
                            "--no-sandbox",
                            "--disable-background-timer-throttling",
                            "--disable-backgrounding-occluded-windows",
                            "--disable-renderer-backgrounding"
                        )));
                
                // Create necessary directories
                java.nio.file.Files.createDirectories(Paths.get(SCREENSHOT_DIR));
                
                System.out.println("✓ Browser session initialized");
            }
            
            // Auto-detect system configuration
            String locale = BrowserConfigUtil.getSystemLocale();
            String timezone = BrowserConfigUtil.getSystemTimezone();
            boolean ignoreHTTPSErrors = BrowserConfigUtil.shouldIgnoreHTTPSErrors();
            String userAgent = BrowserConfigUtil.getUserAgent("chromium");
            
            // Create new browser context with automatic configuration
            context = browser.newContext(new Browser.NewContextOptions()
                    .setIgnoreHTTPSErrors(ignoreHTTPSErrors)
                    .setLocale(locale)
                    .setTimezoneId(timezone)
                    .setUserAgent(userAgent));
            
            // Create new page
            page = context.newPage();
            
            // Set viewport size after page creation to prevent compression
            int[] viewportSize = BrowserConfigUtil.getOptimalViewportSize();
            page.setViewportSize(viewportSize[0], viewportSize[1]);
            
            // Initialize executor
            executor = new PlaywrightWebExecutor(page, SCREENSHOT_DIR);
            
            // Set default timeouts
            page.setDefaultTimeout(30000);
            page.setDefaultNavigationTimeout(60000);
            
            System.out.println("✓ Test session started");
        } catch (Exception e) {
            System.err.println("Failed to setup test session: " + e.getMessage());
            throw new RuntimeException("Test setup failed", e);
        }
    }

    /**
     * Teardown method to be called from @After in Cucumber steps
     */
    public void tearDown() {
        try {
            // Clean up page and context
            if (page != null) {
                page.close();
                page = null;
            }
            if (context != null) {
                context.close();
                context = null;
            }
            
            System.out.println("✓ Test session cleaned");
        } catch (Exception e) {
            System.err.println("Error during test cleanup: " + e.getMessage());
        }
    }

    /**
     * Static cleanup method to be called when all tests are done
     */
    public static void cleanupAll() {
        try {
            if (browser != null) {
                browser.close();
                browser = null;
            }
            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
            System.out.println("✓ Browser session terminated");
        } catch (Exception e) {
            System.err.println("Error during class cleanup: " + e.getMessage());
        }
    }

    /**
     * Get web executor instance
     */
    public PlaywrightWebExecutor getExecutor() {
        return executor;
    }

    /**
     * Get current page instance
     */
    public Page getPage() {
        return page;
    }

    /**
     * Get browser context instance
     */
    public BrowserContext getContext() {
        return context;
    }

    /**
     * Navigate to URL
     */
    public void navigateTo(String url) {
        if (executor != null) {
            executor.navigateTo(url);
        }
    }

    /**
     * Take a screenshot
     */
    public void takeScreenshot(String screenshotName) {
        if (executor != null) {
            executor.takeScreenshot(screenshotName);
        }
    }

    /**
     * Take a screenshot for scenario with timestamp
     */
    public void takeScreenshotForScenario(String scenarioName) {
        if (executor != null) {
            executor.takeScreenshotForScenario(scenarioName);
        }
    }

    /**
     * Wait for page to load
     */
    public void waitForPageLoad() {
        if (executor != null) {
            executor.waitForPageLoad();
        }
    }
}
