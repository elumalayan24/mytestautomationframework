package com.myautomation.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.Test;

import java.io.File;

public class SimplePathTest {

    @Test
    public void testScreenshotPath() {
        // Create a fake screenshot file
        File screenshotDir = new File("test-output/screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
        
        File testScreenshot = new File("test-output/screenshots/test_screenshot.png");
        try {
            // Create a dummy screenshot file
            java.nio.file.Files.write(testScreenshot.toPath(), "dummy screenshot data".getBytes());
            
            // Test ExtentReports with relative path
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/path-test-report.html");
            ExtentReports extent = new ExtentReports();
            extent.attachReporter(spark);
            
            ExtentTest test = extent.createTest("Screenshot Path Test");
            
            // Test with absolute path (should not work)
            String absolutePath = testScreenshot.getAbsolutePath();
            System.out.println("Testing absolute path: " + absolutePath);
            test.fail("Absolute path test").addScreenCaptureFromPath(absolutePath);
            
            // Test with relative path from reports directory (should work)
            String relativePath = "../screenshots/" + testScreenshot.getName();
            System.out.println("Testing relative path from reports: " + relativePath);
            test.pass("Relative path test").addScreenCaptureFromPath(relativePath);
            
            extent.flush();
            System.out.println("✅ Report generated. Check test-output/path-test-report.html");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
