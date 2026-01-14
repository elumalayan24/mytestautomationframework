package com.myautomation.hooks;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.myautomation.reports.listeners.ExtentReportListener;
import com.myautomation.utils.LogCaptureUtil;
import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.myautomation.core.drivers.DriverManager;

import java.io.File;
import java.io.IOException;
import com.aventstack.extentreports.ExtentReports;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class CucumberHooks {
    private static final ThreadLocal<String> scenarioName = new ThreadLocal<>();

    @BeforeAll
    public static void beforeAll() {
        try {
            // Initialize ExtentReports before any tests run
            System.out.println("\n=== Initializing ExtentReports ===");
            ExtentReports extent = ExtentReportListener.createInstance();
            String testSuiteId = ExtentReportListener.getTestSuiteId();
            System.out.println("=== ExtentReports Initialized Successfully ===");
            System.out.println("Test Suite ID: " + testSuiteId);
            System.out.println("Report will be saved to: " + 
                System.getProperty("user.dir") + File.separator + "test-output");
            System.out.println("=========================================\n");
        } catch (Exception e) {
            System.err.println("Failed to initialize ExtentReports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        // Initialize WebDriver before each scenario
        WebDriver driver = DriverManager.getDriver();
        
        String name = scenario.getName();
        scenarioName.set(name);

        // Start log capture
        LogCaptureUtil.startCapture();
        LogCaptureUtil.log("=== Scenario Started: " + name + " ===");

        // Create a test in ExtentReport for this scenario
        ExtentReportListener.createTest(name, "Scenario: " + name);

        // Log scenario start
        ExtentReportListener.getTest().info("<b>Scenario Started:</b> " + name);

        // Log scenario tags if any
        if (!scenario.getSourceTagNames().isEmpty()) {
            String tags = String.join(", ", scenario.getSourceTagNames());
            ExtentReportListener.getTest().info("<b>Tags:</b> " + tags);
            LogCaptureUtil.log("Tags: " + tags);
        }
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                // Take screenshot after every step
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

                // Create timestamp for unique filename
                String timestamp = String.valueOf(System.currentTimeMillis());
                String stepStatus = scenario.isFailed() ? "FAILED" : "PASSED";
                String screenshotName = String.format("step_%s_%s_%s.png",
                        stepStatus.toLowerCase(),
                        scenario.getName().replaceAll("[^a-zA-Z0-9]", "_"),
                        timestamp
                );

                // Save screenshot to file system for ExtentReports
                Path screenshotDir = Paths.get("test-output/screenshots");
                Files.createDirectories(screenshotDir);
                Path screenshotPath = screenshotDir.resolve(screenshotName);
                Files.write(screenshotPath, screenshot);

                // Attach to Cucumber report
                scenario.attach(screenshot, "image/png", screenshotName);

                // Attach to ExtentReport with relative path
                String relativePath = "screenshots/" + screenshotName;
                ExtentReportListener.getTest().info("Step Screenshot:")
                        .addScreenCaptureFromPath(relativePath);

                LogCaptureUtil.log("Screenshot captured: " + screenshotName);

                // Add step status
                if (scenario.isFailed()) {
                    ExtentReportListener.getTest().log(Status.FAIL,
                            MarkupHelper.createLabel("Step Failed", com.aventstack.extentreports.markuputils.ExtentColor.RED));
                    LogCaptureUtil.log("❌ Step FAILED");
                } else {
                    ExtentReportListener.getTest().log(Status.PASS, "Step Passed ✓");
                    LogCaptureUtil.log("✓ Step passed");
                }
            } else {
                LogCaptureUtil.log("⚠️ WebDriver is null - cannot capture screenshot");
            }
        } catch (Exception e) {
            LogCaptureUtil.log("Error in afterStep: " + e.getMessage());
            ExtentReportListener.getTest().warning("Failed to capture step details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        try {
            // Add all captured logs to ExtentReport
            LogCaptureUtil.addLogsToReport(ExtentReportListener.getTest());

            // Take final screenshot
            WebDriver driver = DriverManager.getDriver();
            if (driver != null && scenario.isFailed()) {
                LogCaptureUtil.log("Taking failure screenshot...");

                // Take screenshot
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

                // Save screenshot to file
                String fileName = "failure_" + System.currentTimeMillis() + ".png";
                Path screenshotDir = Paths.get("test-output/screenshots");
                Files.createDirectories(screenshotDir);
                Path destination = screenshotDir.resolve(fileName);
                Files.write(destination, screenshot);

                // Attach to Cucumber report
                scenario.attach(screenshot, "image/png", fileName);

                // Attach to ExtentReport
                String relativePath = "screenshots/" + fileName;
                ExtentReportListener.getTest().fail("Scenario Failed - Screenshot:")
                        .addScreenCaptureFromPath(relativePath);

                LogCaptureUtil.log("Failure screenshot saved: " + fileName);
            }

            // Log scenario result
            if (scenario.isFailed()) {
                LogCaptureUtil.log("=== SCENARIO FAILED: " + scenario.getName() + " ===");
                ExtentReportListener.getTest().log(Status.FAIL,
                        MarkupHelper.createLabel("Scenario Failed", com.aventstack.extentreports.markuputils.ExtentColor.RED));

                // Log failure reason if available
                if (scenario.getStatus() != null) {
                    String failureReason = "Status: " + scenario.getStatus().name();
                    ExtentReportListener.getTest().fail(failureReason);
                    LogCaptureUtil.log("Failure reason: " + failureReason);
                }
            } else {
                LogCaptureUtil.log("=== SCENARIO PASSED: " + scenario.getName() + " ===");
                ExtentReportListener.getTest().log(Status.PASS,
                        MarkupHelper.createLabel("Scenario Passed", com.aventstack.extentreports.markuputils.ExtentColor.GREEN));
            }

            // Add final logs to report
            LogCaptureUtil.addLogsToReport(ExtentReportListener.getTest());
        } catch (Exception e) {
            System.err.println("Error in afterScenario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @AfterAll
    public static void cleanup() {
        try {
            System.out.println("=== Starting cleanup after all scenarios ===");
            // Flush ExtentReports
            if (ExtentReportListener.getExtent() != null) {
                System.out.println("Flushing ExtentReports...");
                ExtentReportListener.getExtent().flush();
                System.out.println("ExtentReports flushed successfully");
                
                // Print the report location
                String reportPath = System.getProperty("user.dir") + File.separator + "test-output" + File.separator + "ExtentReport_*.html";
                System.out.println("Report should be available at: " + reportPath);
                
                // List all report files
                File dir = new File(System.getProperty("user.dir") + File.separator + "test-output");
                if (dir.exists() && dir.isDirectory()) {
                    System.out.println("Contents of test-output directory:");
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".html"));
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
                        }
                    } else {
                        System.out.println("  No HTML report files found in test-output directory");
                    }
                } else {
                    System.out.println("test-output directory does not exist: " + dir.getAbsolutePath());
                }
            } else {
                System.out.println("ExtentReports instance is null, nothing to flush");
            }
            
            // Log any final messages
            System.out.println("=== Cleanup completed ===");
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}