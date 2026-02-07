package com.myautomation.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.myautomation.reporting.ExtentManager;
import com.myautomation.reporting.ExtentTestManager;
import com.myautomation.sessions.PlaywrightCucumberSession;
import com.myautomation.sessions.PlaywrightSessionHolder;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Test;

import java.io.File;

public class CucumberScreenshotTest {

    private PlaywrightCucumberSession session;

    @Test
    public void testCucumberScreenshotFlow() {
        // Simulate the complete Cucumber flow with ReportingHooks
        
        // 1. Setup ExtentTest (like ReportingHooks @Before)
        ExtentReports extent = ExtentManager.getInstance();
        ExtentTest test = extent.createTest("Test Cucumber Screenshot Display");
        ExtentTestManager.setTest(test);
        
        // 2. Setup Playwright session (like PlaywrightHooks @Before)
        session = new PlaywrightCucumberSession();
        session.setUp();
        PlaywrightSessionHolder.setSession(session);
        
        try {
            // 3. Execute test steps
            session.navigateTo("https://www.example.com");
            System.out.println("✅ Navigated to example.com");
            
            // 4. Take screenshot (like PlaywrightHooks @After)
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String screenshotName = "cucumber_test_" + timestamp;
            session.takeScreenshot(screenshotName);
            System.out.println("✅ Screenshot taken: " + screenshotName);
            
            // 5. Simulate ReportingHooks @After logic
            String screenshotPath = findLatestPlaywrightScreenshot("Test Cucumber Screenshot Display");
            System.out.println("Found screenshot path: " + screenshotPath);
            
            if (screenshotPath != null) {
                test.pass("Scenario Passed")
                        .addScreenCaptureFromPath(screenshotPath);
                System.out.println("✅ Screenshot attached to report: " + screenshotPath);
            } else {
                test.pass("Scenario Passed");
                System.out.println("❌ No screenshot found to attach");
            }
            
        } catch (Exception e) {
            test.fail("Scenario Failed: " + e.getMessage());
            System.err.println("❌ Test failed: " + e.getMessage());
        } finally {
            // 6. Cleanup (like hooks @After)
            if (session != null) {
                session.tearDown();
            }
            PlaywrightSessionHolder.clearSession();
            ExtentTestManager.unload();
            extent.flush();
            System.out.println("✅ Report generated successfully");
        }
    }
    
    private String findLatestPlaywrightScreenshot(String scenarioName) {
        try {
            File screenshotsDir = new File("test-output/screenshots");
            if (!screenshotsDir.exists() || !screenshotsDir.isDirectory()) {
                return null;
            }

            File[] screenshotFiles = screenshotsDir.listFiles((dir, name) -> 
                name.contains("cucumber_test") && name.endsWith(".png"));
            
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
            
            // Return relative path from reports directory
            if (latestFile != null) {
                String absolutePath = latestFile.getAbsolutePath();
                System.out.println("Absolute path: " + absolutePath);
                
                String relativePath = "../screenshots/" + latestFile.getName();
                System.out.println("Relative path for report: " + relativePath);
                
                return relativePath;
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error finding latest Playwright screenshot: " + e.getMessage());
            return null;
        }
    }
}
