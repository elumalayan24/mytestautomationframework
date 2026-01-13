package com.myautomation.core;

import com.myautomation.config.ConfigManager;
import com.myautomation.utils.ActionUtils;
import com.myautomation.utils.HtmlReportManager;
import com.microsoft.playwright.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Base test class that provides common setup and teardown methods for all tests.
 */
public class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = ConfigManager.getProperty("base.url");
    private static final boolean HEADLESS = Boolean.parseBoolean(ConfigManager.getProperty("headless", "false"));
    private static final String BROWSER_TYPE = ConfigManager.getProperty("browser.type", "chrome");
    
    // Playwright related fields
    protected Playwright playwright;
    protected Browser browser;
    protected Page page;
    protected BrowserContext context;
    protected BrowserContext browserContext;
    
    // Log capture and output redirection fields
    private ByteArrayOutputStream logCapture;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    // Configuration
    protected static final Properties config = loadConfig();
    
    @BeforeSuite
    public void beforeSuite() {
        logger.info("=== Test Suite Started ===");
    }
    
    @BeforeMethod
    public void setup(Method method) {
        String testName = method.getName();
        logger.info("=== Starting Test: {} ===", testName);
        
        // Redirect System.out and System.err to capture logs
        logCapture = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(logCapture);
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(printStream);
        System.setErr(printStream);
        
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
        String screenshotPath = null;
        
        // Restore original System.out and System.err
        String logs = "";
        if (logCapture != null) {
            logs = logCapture.toString();
            System.setOut(originalOut);
            System.setErr(originalErr);
            try {
                logCapture.close();
            } catch (IOException e) {
                logger.error("Failed to close log capture stream: {}", e.getMessage());
            }
        }
        
        // Take screenshot on test failure
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test failed: {}", testName);
            String screenshotName = testName + "_" + System.currentTimeMillis();
            
            try {
                if (page != null) {
                    screenshotPath = ActionUtils.takeScreenshot(page, screenshotName);
                } else if (driver != null) {
                    screenshotPath = ActionUtils.takeScreenshot(driver, screenshotName);
                }
                
                // If screenshot was taken, copy it to the report directory
                if (screenshotPath != null && Files.exists(Paths.get(screenshotPath))) {
                    String destPath = HtmlReportManager.REPORT_DIR + "/screenshots/" + new File(screenshotPath).getName();
                    Files.copy(Paths.get(screenshotPath), Paths.get(destPath), 
                             java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    screenshotPath = destPath;
                }
            } catch (Exception e) {
                logger.error("Failed to capture or copy screenshot: {}", e.getMessage());
            }
        }
        
        // Log test results to HTML report
        HtmlReportManager.logTest(result, screenshotPath, logs);
        
        // Close browser and cleanup
        cleanup();
        logger.info("=== Completed Test: {} ===\n", testName);
    }
    
    @AfterSuite
    public void afterSuite() {
        // Generate the HTML report
        HtmlReportManager.generateReport();
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
        try {
            playwright = Playwright.create();
            
            // Create browser instance based on configuration
            BrowserType browserType = getBrowserType();
            browser = browserType.launch(new BrowserType.LaunchOptions()
                    .setHeadless(HEADLESS)
                    .setSlowMo(100)  // Slow down execution for better visibility
            );
            
            // Create a new browser context and page
            context = browser.newContext();
            page = context.newPage();
            
            logger.info("Navigating to: {}", BASE_URL);
            page.navigate(BASE_URL);
        } catch (Exception e) {
            logger.error("Failed to initialize Playwright", e);
            throw new RuntimeException("Failed to initialize Playwright", e);
        }
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
