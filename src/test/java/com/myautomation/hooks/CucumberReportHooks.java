package com.myautomation.hooks;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.myautomation.utils.LogCaptureUtil;
import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.myautomation.core.drivers.DriverManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

public class CucumberReportHooks {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static ExtentReports extent;
    private static String testSuiteId;
    private static boolean suiteInitialized = false;

    private static String generateTestSuiteId() {
        return "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
               System.currentTimeMillis() % 10000;
    }

    @Before(order = 0)
    public static void beforeAll(Scenario scenario) {
        if (!suiteInitialized) {
            // Generate test suite ID immediately - this is the ONLY source
            testSuiteId = generateTestSuiteId();
            System.setProperty("test.suite.id", testSuiteId);
            
            // Print test suite ID to console
            System.out.println("TEST SUITE ID: " + testSuiteId);
            
            suiteInitialized = true;
            
            // Create test-output directory if it doesn't exist
            File reportDir = new File("test-output");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String reportPath = "test-output/ExtentReport_" + timeStamp + ".html";
            
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("SauceDemo Test Automation Report");
            spark.config().setReportName("SauceDemo Test Execution - " + testSuiteId);
            
            extent = new ExtentReports();
            extent.attachReporter(spark);
            
            // Set system information including test suite ID
            extent.setSystemInfo("Test Suite ID", testSuiteId);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("OS Version", System.getProperty("os.version"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("Report Generated On", new SimpleDateFormat("MMM dd, yyyy hh:mm a").format(new Date()));
        }
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        LogCaptureUtil.startCapture();
        LogCaptureUtil.log("Starting scenario: " + scenario.getName());

        if (driver.get() == null) {
            driver.set(DriverManager.getDriver());
        }

        ExtentTest test = extent.createTest(scenario.getName());
        extentTest.set(test);
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        LogCaptureUtil.addLogsToReport(extentTest.get());

        if (scenario.isFailed()) {
            WebDriver currentDriver = driver.get();
            if (currentDriver != null) {
                try {
                    byte[] screenshot = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.BYTES);
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String screenshotName = String.format("screenshot_failed_%s_%s.png",
                            scenario.getName().replaceAll("\\s+", "_"),
                            timestamp
                    );

                    scenario.attach(screenshot, "image/png", screenshotName);
                    LogCaptureUtil.log("Step failed: " + scenario.getStatus().name());
                } catch (Exception e) {
                    LogCaptureUtil.log("Error capturing screenshot: " + e.getMessage());
                }
            }
            extentTest.get().log(Status.FAIL, "Step Failed");
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                LogCaptureUtil.log("SCENARIO FAILED: " + scenario.getName());
                extentTest.get().log(Status.FAIL, "Scenario Failed");
            } else {
                LogCaptureUtil.log("SCENARIO PASSED: " + scenario.getName());
                extentTest.get().log(Status.PASS, "Scenario Passed");
            }

            LogCaptureUtil.addLogsToReport(extentTest.get());

        } catch (Exception e) {
            LogCaptureUtil.log("Error in afterScenario: " + e.getMessage());
        } finally {
            // Clean up browser after each scenario
            LogCaptureUtil.log("Closing browser after scenario: " + scenario.getName());
            cleanupDriver();
            LogCaptureUtil.stopCapture();
        }
    }

    @After(order = 1000)
    public void afterAll(Scenario scenario) {
        try {
            LogCaptureUtil.log("=== Final cleanup after all test scenarios ===");
            
            // Flush ExtentReports
            if (extent != null) {
                extent.flush();
                LogCaptureUtil.log("ExtentReports flushed successfully");
            }
            
            // Ensure any remaining browser instances are closed
            try {
                DriverManager.unload();
                LogCaptureUtil.log("Final browser cleanup completed");
            } catch (Exception e) {
                LogCaptureUtil.log("No additional browser instances to clean up");
            }
            
            LogCaptureUtil.log("=== All cleanup completed ===");
        } catch (Exception e) {
            LogCaptureUtil.log("Error in afterAll cleanup: " + e.getMessage());
        }
    }

    private void cleanupDriver() {
        try {
            LogCaptureUtil.log("Cleaning up WebDriver instance...");
            DriverManager.unload();
            LogCaptureUtil.log("WebDriver cleanup completed");
        } catch (Exception e) {
            LogCaptureUtil.log("Error while closing WebDriver: " + e.getMessage());
        }
    }
}