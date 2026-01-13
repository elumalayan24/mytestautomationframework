package com.myautomation.hooks;

import com.aventstack.extentreports.Status;
import com.myautomation.listeners.ExtentReportListener;
import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.myautomation.utils.DriverManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CucumberHooks {
    private static final ThreadLocal<String> scenarioName = new ThreadLocal<>();
    
    @BeforeAll
    public static void beforeAll() {
        // Initialize ExtentReports before any tests run
        ExtentReportListener.createInstance();
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        String name = scenario.getName();
        scenarioName.set(name);
        
        // Create a test in ExtentReport for this scenario
        ExtentReportListener.createTest(name, "Scenario: " + name);
        
        // Log scenario start
        ExtentReportListener.getTest().info("<b>Scenario Started:</b> " + name);
        
        // Log scenario tags if any
        if (!scenario.getSourceTagNames().isEmpty()) {
            ExtentReportListener.getTest().info("<b>Tags:</b> " + String.join(", ", scenario.getSourceTagNames()));
        }
    }
    
    @AfterStep
    public void afterStep(Scenario scenario) {
        // You can add step logging here if needed
    }
    
    @After
    public void afterScenario(Scenario scenario) {
        // Take screenshot if scenario failed
        if (scenario.isFailed()) {
            try {
                WebDriver driver = DriverManager.getDriver();
                if (driver != null) {
                    // Take screenshot
                    File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    
                    // Save screenshot to file
                    String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
                    Path destination = Paths.get("test-output/screenshots", fileName);
                    Files.createDirectories(destination.getParent());
                    Files.copy(screenshot.toPath(), destination);
                    
                    // Add screenshot to report
                    ExtentReportListener.getTest().addScreenCaptureFromPath(
                        "screenshots/" + fileName,
                        "Screenshot on failure"
                    );
                }
            } catch (Exception e) {
                ExtentReportListener.getTest().warning("Failed to capture screenshot: " + e.getMessage());
            }
            
            // Log the failure
            ExtentReportListener.getTest().log(Status.FAIL, "Scenario Failed: " + scenario.getName());
            // Log the exception if available
            if (scenario.getStatus() == io.cucumber.java.Status.FAILED) {
                scenario.log("Failure details: " + scenario.getStatus().name());
            }
        } else {
            ExtentReportListener.getTest().log(Status.PASS, "Scenario Passed");
        }
        
        // Log scenario end
        ExtentReportListener.getTest().info("<b>Scenario Finished:</b> " + scenario.getName() + 
                                         " - Status: " + (scenario.isFailed() ? "FAILED" : "PASSED"));
        
        // Flush the report
        ExtentReportListener.getExtent().flush();
    }
}
