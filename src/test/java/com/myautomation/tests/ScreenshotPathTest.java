package com.myautomation.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.myautomation.sessions.PlaywrightCucumberSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ScreenshotPathTest {

    private PlaywrightCucumberSession session;

    @BeforeEach
    public void setUp() {
        session = new PlaywrightCucumberSession();
        session.setUp();
    }

    @Test
    public void testScreenshotPath() {
        // Navigate to a page
        session.navigateTo("https://www.example.com");
        
        // Take a screenshot
        session.takeScreenshot("screenshot_path_test");
        
        // Verify screenshot file exists
        File screenshotFile = new File("test-output/screenshots/screenshot_path_test_20260207_184900.png");
        if (screenshotFile.exists()) {
            System.out.println("✅ Screenshot created at: " + screenshotFile.getAbsolutePath());
            
            // Test ExtentReports with relative path
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/screenshot-test-report.html");
            ExtentReports extent = new ExtentReports();
            extent.attachReporter(spark);
            
            ExtentTest test = extent.createTest("Screenshot Path Test");
            String relativePath = "screenshots/" + screenshotFile.getName();
            test.pass("Screenshot test").addScreenCaptureFromPath(relativePath);
            
            extent.flush();
            System.out.println("✅ Report generated with relative path: " + relativePath);
        } else {
            System.err.println("❌ Screenshot file not found!");
        }
    }

    @AfterEach
    public void tearDown() {
        if (session != null) {
            session.tearDown();
        }
    }
}
