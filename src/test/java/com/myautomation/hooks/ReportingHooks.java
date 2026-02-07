package com.myautomation.hooks;

import com.aventstack.extentreports.ExtentTest;
import com.myautomation.reporting.ExtentManager;
import com.myautomation.reporting.ExtentTestManager;
import com.myautomation.sessions.PlaywrightCucumberSession;
import com.myautomation.utils.ErrorLogger;
import com.myautomation.utils.ScreenshotUtil;
import com.myautomation.core.drivers.DriverFactory;
import com.myautomation.sessions.PlaywrightSessionHolder;
import com.myautomation.database.DatabaseService;
import com.myautomation.database.FailureReasonService;
import io.cucumber.java.*;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import io.appium.java_client.AppiumDriver;

import java.io.File;

public class ReportingHooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        ExtentTest test = ExtentManager
                .getInstance()
                .createTest(scenario.getName());

        ExtentTestManager.setTest(test);
        ErrorLogger.info("Scenario started");
        
        // Log to database
        DatabaseService.logInfo("ExtentReport Test Created", 
            "Scenario: " + scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        ExtentTest test = ExtentTestManager.getTest();
        if (test != null) {
            try {
                String engine = com.myautomation.config.ConfigManager.getProperty("engine", "selenium").toLowerCase();
                String testSuiteId = DatabaseService.getCurrentTestSuiteId();
                
                if (scenario.isFailed()) {
                    logTestFailure(scenario, engine, testSuiteId);
                }
                
                if (scenario.getSourceTagNames().contains("@playwright")) {
                    // Take automatic screenshot for Playwright scenarios
                    PlaywrightCucumberSession session = PlaywrightSessionHolder.getSession();
                    if (session != null) {
                        session.takeScreenshotForScenario(scenario.getName());
                    }
                    
                    // Handle Playwright scenarios - find the latest screenshot
                    String screenshotPath = findLatestPlaywrightScreenshot(scenario.getName());
                    if (screenshotPath != null) {
                        // Add screenshot to Cucumber HTML report
                        try {
                            byte[] screenshotData = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(screenshotPath));
                            scenario.attach(screenshotData, "image/png", "Screenshot");
                            System.out.println("📸 Screenshot embedded in Cucumber report: " + screenshotPath);
                            
                            // Log screenshot to database
                            DatabaseService.logInfo("Screenshot Captured", 
                                "Playwright screenshot: " + screenshotPath);
                        } catch (Exception e) {
                            System.err.println("Failed to embed screenshot in Cucumber report: " + e.getMessage());
                        }
                        
                        // Add screenshot to ExtentReports
                        if (scenario.isFailed()) {
                            test.fail("Scenario Failed")
                                    .addScreenCaptureFromPath(screenshotPath);
                        } else {
                            test.pass("Scenario Passed")
                                    .addScreenCaptureFromPath(screenshotPath);
                        }
                        System.out.println("📸 Playwright screenshot attached to ExtentReports: " + screenshotPath);
                    } else {
                        if (scenario.isFailed()) {
                            test.fail("Scenario Failed");
                        } else {
                            test.pass("Scenario Passed");
                        }
                    }
                } else {
                    // Handle Selenium WebDriver scenarios
                    Object driverObj = DriverFactory.getDriver();
                    if (driverObj instanceof WebDriver driver) {
                        String timestamp = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String screenshotName = scenario.getName() + "_" + timestamp;
                        String screenshotPath = ScreenshotUtil.capture(driver, screenshotName);
                        
                        if (screenshotPath != null) {
                            // Use absolute path to ensure screenshots display correctly in HTML reports
                            String absolutePath = new File(screenshotPath).getAbsolutePath();
                            System.out.println("Selenium screenshot - Using absolute path: " + absolutePath);
                            
                            // Add screenshot to Cucumber HTML report
                            try {
                                byte[] screenshotData = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(absolutePath));
                                scenario.attach(screenshotData, "image/png", "Screenshot");
                                System.out.println("📸 Selenium screenshot embedded in Cucumber report: " + absolutePath);
                            } catch (Exception e) {
                                System.err.println("Failed to embed Selenium screenshot in Cucumber report: " + e.getMessage());
                            }
                            
                            // Add screenshot to ExtentReports
                            if (scenario.isFailed()) {
                                test.fail("Scenario Failed")
                                        .addScreenCaptureFromPath(absolutePath);
                            } else {
                                test.pass("Scenario Passed")
                                        .addScreenCaptureFromPath(absolutePath);
                            }
                            System.out.println("📸 Selenium screenshot attached to ExtentReports: " + absolutePath);
                        } else {
                            if (scenario.isFailed()) {
                                test.fail("Scenario Failed");
                            } else {
                                test.pass("Scenario Passed");
                            }
                        }
                    } else {
                        // Handle Mobile App scenarios
                        if (driverObj instanceof AppiumDriver mobileDriver) {
                            String timestamp = java.time.LocalDateTime.now()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                            String screenshotName = scenario.getName() + "_" + timestamp;
                            
                            try {
                                // Take mobile screenshot
                                byte[] screenshotData = mobileDriver.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                                String screenshotPath = "test-output/screenshots/" + screenshotName + ".png";
                                java.io.File screenshotFile = new java.io.File(screenshotPath);
                                screenshotFile.getParentFile().mkdirs();
                                java.nio.file.Files.write(screenshotFile.toPath(), screenshotData);
                                
                                String absolutePath = screenshotFile.getAbsolutePath();
                                System.out.println("Mobile screenshot saved: " + absolutePath);
                                
                                // Add screenshot to Cucumber HTML report
                                scenario.attach(screenshotData, "image/png", "Mobile Screenshot");
                                System.out.println("📸 Mobile screenshot embedded in Cucumber report: " + absolutePath);
                                
                                // Log screenshot to database
                                DatabaseService.logInfo("Screenshot Captured", 
                                    "Mobile screenshot: " + absolutePath);
                                
                                // Add screenshot to ExtentReports
                                if (scenario.isFailed()) {
                                    test.fail("Scenario Failed")
                                            .addScreenCaptureFromPath(absolutePath);
                                } else {
                                    test.pass("Scenario Passed")
                                            .addScreenCaptureFromPath(absolutePath);
                                }
                                System.out.println("📸 Mobile screenshot attached to ExtentReports: " + absolutePath);
                            } catch (Exception e) {
                                System.err.println("Failed to take mobile screenshot: " + e.getMessage());
                                if (scenario.isFailed()) {
                                    test.fail("Scenario Failed");
                                } else {
                                    test.pass("Scenario Passed");
                                }
                            }
                        } else {
                            System.err.println("WebDriver is null or not available, unable to take screenshot");
                            if (scenario.isFailed()) {
                                test.fail("Scenario Failed");
                            } else {
                                test.pass("Scenario Passed");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to handle screenshot: " + e.getMessage());
                if (scenario.isFailed()) {
                    test.fail("Scenario Failed");
                } else {
                    test.pass("Scenario Passed");
                }
            }
        }

        ExtentTestManager.unload();
        ExtentManager.getInstance().flush();
    }
    
    private void logTestFailure(Scenario scenario, String engine, String testSuiteId) {
        try {
            // Get page URL and browser info
            String pageUrl = "";
            String browserInfo = "";
            
            try {
                Object driverObj = DriverFactory.getDriver();
                if (driverObj instanceof com.microsoft.playwright.Page playwrightPage) {
                    pageUrl = playwrightPage.url();
                    browserInfo = "Playwright";
                } else if (driverObj instanceof WebDriver seleniumDriver) {
                    pageUrl = seleniumDriver.getCurrentUrl();
                    browserInfo = seleniumDriver.toString();
                }
            } catch (Exception e) {
                System.err.println("Could not get page URL/browser info: " + e.getMessage());
            }
            
            // Determine failure type based on scenario name or error
            String failureType = "TEST_FAILURE";
            String errorMessage = "Scenario failed: " + scenario.getName();
            
            // Log to failure reasons table
            FailureReasonService.logFailureReason(
                testSuiteId,
                scenario.getName(),
                failureType,
                errorMessage,
                null, // stack trace
                null, // screenshot path - will be added separately
                null, // element locator
                null, // expected value
                null, // actual value
                pageUrl,
                browserInfo,
                engine
            );
            
            System.out.println("🔴 Failure logged to database: " + failureType + " for " + scenario.getName());
            
        } catch (Exception e) {
            System.err.println("Failed to log test failure: " + e.getMessage());
        }
    }

    @AfterAll
    public static void afterAll() {
        // Get the current engine type from configuration
        String engine = com.myautomation.config.ConfigManager.getProperty("engine", "selenium").toLowerCase();
        
        // Close drivers based on what was actually used
        try {
            Object driverObj = DriverFactory.getDriver();
            if (driverObj instanceof org.openqa.selenium.WebDriver driver) {
                System.out.println("Closing Selenium WebDriver after all tests...");
                driver.quit();
                System.out.println("Selenium WebDriver closed successfully.");
            } else if (driverObj instanceof io.appium.java_client.AppiumDriver mobileDriver) {
                System.out.println("Closing Mobile Driver after all tests...");
                mobileDriver.quit();
                System.out.println("Mobile Driver closed successfully.");
            } else if (driverObj instanceof com.microsoft.playwright.Page playwrightPage) {
                System.out.println("Closing Playwright session after all tests...");
                try {
                    // Close the Playwright browser context and page
                    playwrightPage.context().browser().close();
                    System.out.println("Playwright session closed successfully.");
                } catch (Exception e) {
                    System.err.println("Error closing Playwright session: " + e.getMessage());
                }
            } else {
                System.out.println("No active driver found to close for " + engine + " engine.");
            }
        } catch (Exception e) {
            System.err.println("Error closing " + engine + " driver: " + e.getMessage());
        }
        
        // Also cleanup any remaining resources
        try {
            DriverFactory.unload();
            System.out.println("Driver factory cleanup completed for " + engine + " engine.");
        } catch (Exception e) {
            System.err.println("Error during driver factory cleanup: " + e.getMessage());
        }
    }

    /**
     * Find the latest screenshot for a given scenario name
     */
    private String findLatestPlaywrightScreenshot(String scenarioName) {
        try {
            File screenshotsDir = new File("test-output/screenshots");
            if (!screenshotsDir.exists() || !screenshotsDir.isDirectory()) {
                System.out.println("Screenshots directory does not exist: " + screenshotsDir.getAbsolutePath());
                return null;
            }

            File[] allPngFiles = screenshotsDir.listFiles((dir, name) -> name.endsWith(".png"));

            // Look for files that start with "playwright_" + scenarioName
            File[] screenshotFiles = screenshotsDir.listFiles((dir, name) -> 
                name.startsWith("playwright_" + scenarioName) && name.endsWith(".png"));
            
            if (screenshotFiles == null || screenshotFiles.length == 0) {
                return null;
            }

            // Find the most recent file
            File latestFile = null;
            long lastModified = 0;
            
            for (File file : screenshotFiles) {
                if (file.lastModified() > lastModified) {
                    lastModified = file.lastModified();
                    latestFile = file;
                }
            }
            
            // Return absolute path for ExtentReports to ensure proper display
            if (latestFile != null) {
                String absolutePath = latestFile.getAbsolutePath();
                return absolutePath;
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error finding latest Playwright screenshot: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
