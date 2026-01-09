package com.myautomation.core;

import com.microsoft.playwright.*;
import com.myautomation.utils.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

/**
 * Base test class that provides common setup, teardown, and utility methods
 * for all test classes in the framework.
 */
public class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static final Properties config = loadConfig();
    
    // WebDriver and Playwright instances
    protected WebDriver driver;
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext browserContext;
    protected Page page;
    
    // Configuration constants
    protected static final String BASE_URL = config.getProperty("base.url", "https://example.com");
    protected static final boolean HEADLESS = Boolean.parseBoolean(config.getProperty("headless", "true"));
    protected static final String BROWSER_TYPE = config.getProperty("browser.type", "chrome");
    protected static final String ENVIRONMENT = config.getProperty("environment", "staging");
    
    @BeforeSuite
    public void beforeSuite() {
        logger.info("=== Starting Test Suite ===");
        logger.info("Environment: {}", ENVIRONMENT);
        logger.info("Base URL: {}", BASE_URL);
    }
    
    @BeforeMethod
    public void setup(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("=== Starting Test: {} ===", testName);
        
        // Initialize the appropriate browser based on configuration
        if (config.getProperty("automation.type", "selenium").equalsIgnoreCase("playwright")) {
            initializePlaywright();
        } else {
            initializeSelenium();
        }
    }
    
    @AfterMethod
    public void teardown(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // Take screenshot on test failure
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test failed: {}", testName);
            String screenshotName = testName + "_" + System.currentTimeMillis();
            
            try {
                if (page != null) {
                    ActionUtils.takeScreenshot(page, screenshotName);
                } else if (driver != null) {
                    ActionUtils.takeScreenshot(driver, screenshotName);
                }
            } catch (Exception e) {
                logger.error("Failed to capture screenshot: {}", e.getMessage());
            }
        }
        
        // Close browser and cleanup
        cleanup();
        logger.info("=== Completed Test: {} ===\n", testName);
    }
    
    @AfterSuite
    public void afterSuite() {
        logger.info("=== Test Suite Completed ===");
    }
    
    private void initializeSelenium() {
        logger.info("Initializing Selenium WebDriver");
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) {
            options.addArguments("--headless=new");
        }
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        
        logger.info("Navigating to: {}", BASE_URL);
        driver.get(BASE_URL);
    }
    
    private void initializePlaywright() {
        logger.info("Initializing Playwright");
        playwright = Playwright.create();
        
        // Create browser instance based on configuration
        BrowserType browserType = getBrowserType();
        browser = browserType.launch(new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS)
                .setSlowMo(100)  // Slow down execution for better visibility
        );
        
        // Create a new browser context
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setRecordVideoDir(Paths.get("test-results/videos/"))
        );
        
        // Create a new page
        page = browserContext.newPage();
        
        logger.info("Navigating to: {}", BASE_URL);
        page.navigate(BASE_URL);
    }
    
    private BrowserType getBrowserType() {
        return switch (BROWSER_TYPE.toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }
    
    private void cleanup() {
        try {
            // Close Playwright resources
            if (page != null) {
                page.close();
            }
            if (browserContext != null) {
                browserContext.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
            
            // Close Selenium WebDriver
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage());
        }
    }
    
    private static Properties loadConfig() {
        Properties props = new Properties();
        String configFile = "config.properties";
        
        try (InputStream input = BaseTest.class.getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                logger.warn("Unable to find {}", configFile);
                return props;
            }
            props.load(input);
        } catch (IOException e) {
            logger.error("Error loading configuration: {}", e.getMessage());
        }
        
        // Override with system properties if they exist
        props.putAll(System.getProperties());
        return props;
    }
    
    // Common utility methods that can be used by all test classes
    
    /**
     * Wait for an element to be visible (Selenium)
     */
    protected void waitForElementVisible(org.openqa.selenium.By locator) {
        ActionUtils.waitForVisibility(driver, locator, Duration.ofSeconds(10));
    }
    
    /**
     * Wait for an element to be visible (Playwright)
     */
    protected void waitForElementVisible(String selector) {
        ActionUtils.waitForVisibility(page, selector, Duration.ofSeconds(10));
    }
    
    /**
     * Take a screenshot with the given name
     */
    protected void takeScreenshot(String name) {
        if (page != null) {
            ActionUtils.takeScreenshot(page, name);
        } else if (driver != null) {
            ActionUtils.takeScreenshot(driver, name);
        }
    }
}
