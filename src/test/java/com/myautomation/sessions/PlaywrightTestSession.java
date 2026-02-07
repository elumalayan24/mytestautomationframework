package com.myautomation.sessions;

import com.microsoft.playwright.*;
import com.myautomation.utils.BrowserConfigUtil;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

/**
 * JUnit Test Session class for managing Playwright browser lifecycle
 * Provides automatic setup and teardown of browser, context, and page objects
 * Only contains session management methods - web operations are handled by PlaywrightWebExecutor
 */
public class PlaywrightTestSession {

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected PlaywrightWebExecutor executor;

    // Configuration options
    private static final boolean HEADLESS = false;
    private static final boolean TRACING = true;
    private static final String TRACING_DIR = "test-output/traces/";
    private static final String SCREENSHOT_DIR = "test-output/screenshots/";

    @BeforeAll
    static void setupClass() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(HEADLESS)
                    .setSlowMo(100)
                    .setArgs(java.util.Arrays.asList("--start-maximized", "--disable-infobars")));
            
            // Create necessary directories
            java.nio.file.Files.createDirectories(Paths.get(TRACING_DIR));
            java.nio.file.Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            
            System.out.println("✓ Browser session initialized");
        } catch (Exception e) {
            System.err.println("Failed to initialize browser session: " + e.getMessage());
            throw new RuntimeException("Browser setup failed", e);
        }
    }

    @BeforeEach
    void setupTest(TestInfo testInfo) {
        try {
            String testName = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9]", "_");
            
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
            
            // Start tracing for this test
            if (TRACING) {
                context.tracing().start(new Tracing.StartOptions()
                        .setName(testName)
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true));
            }
            
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
            
            System.out.println("✓ Test session started: " + testName);
        } catch (Exception e) {
            System.err.println("Failed to setup test session: " + e.getMessage());
            throw new RuntimeException("Test setup failed", e);
        }
    }

    @AfterEach
    void tearDownTest(TestInfo testInfo) {
        String testName = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9]", "_");
        
        try {
            // Take screenshot on test failure
            if (testInfo.getTags().contains("FAILED") || page == null) {
                executor.takeScreenshot(testName + "_failure");
            }
            
            // Stop and save tracing
            if (TRACING && context != null) {
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(Paths.get(TRACING_DIR + testName + ".zip")));
            }
            
            // Clean up page and context
            if (page != null) {
                page.close();
                page = null;
            }
            if (context != null) {
                context.close();
                context = null;
            }
            
            System.out.println("✓ Test session cleaned: " + testName);
        } catch (Exception e) {
            System.err.println("Error during test cleanup: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDownClass() {
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
    protected PlaywrightWebExecutor getExecutor() {
        return executor;
    }

    /**
     * Get current page instance
     */
    protected Page getPage() {
        return page;
    }

    /**
     * Get browser context instance
     */
    protected BrowserContext getContext() {
        return context;
    }
}
