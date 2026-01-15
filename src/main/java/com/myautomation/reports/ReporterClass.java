package com.myautomation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReporterClass {
    
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static ThreadLocal<String> testCategory = new ThreadLocal<>();
    private static final String REPORT_PATH = System.getProperty("user.dir") + "/test-output/ExtentReports/";
    private static final String SCREENSHOT_PATH = REPORT_PATH + "screenshots/";
    
    /**
     * Initialize ExtentReports with configuration
     */
    public static void initReport() {
        if (extent == null) {
            // Create report directory if it doesn't exist
            new File(REPORT_PATH).mkdirs();
            new File(SCREENSHOT_PATH).mkdirs();
            
            // Initialize ExtentReports
            ExtentSparkReporter htmlReporter = new ExtentSparkReporter(REPORT_PATH + "TestReport_" + getCurrentDateTime() + ".html");
            
            // Configure the report
            htmlReporter.config().setDocumentTitle("Automation Test Report");
            htmlReporter.config().setReportName("Test Automation Report");
            htmlReporter.config().setTheme(Theme.STANDARD);
            htmlReporter.config().setEncoding("utf-8");
            
            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
            
            // Add system information
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User Name", System.getProperty("user.name"));
        }
    }
    
    /**
     * Create a new test in the report
     * @param testName Name of the test
     * @param description Description of the test
     * @param category Test category
     */
    public static void createTest(String testName, String description, String category) {
        ExtentTest extentTest = extent.createTest(testName, description)
                .assignCategory(category);
        test.set(extentTest);
        testCategory.set(category);
        logInfo("Test Started: " + testName);
    }
    
    /**
     * Log an information message
     * @param message Message to log
     */
    public static void logInfo(String message) {
        // Always log to console
        System.out.println("[INFO] " + message);
        
        // Also log to ExtentReports if test is initialized
        if (test.get() != null) {
            test.get().info(message);
        }
    }
    
    /**
     * Log a passed step
     * @param message Message to log
     */
    public static void logPass(String message) {
        if (test.get() != null) {
            test.get().pass(MarkupHelper.createLabel(message, ExtentColor.GREEN));
        }
    }
    
    /**
     * Log a failed step
     * @param message Message to log
     */
    public static void logFail(String message) {
        if (test.get() != null) {
            test.get().fail(message);
        }
    }
    
    /**
     * Log a warning message
     * @param message Message to log
     */
    public static void logWarning(String message) {
        if (test.get() != null) {
            test.get().warning(MarkupHelper.createLabel(message, ExtentColor.ORANGE));
        }
    }
    
    /**
     * Log an error message
     * @param message Message to log
     */
    public static void logError(String message) {
        if (test.get() != null) {
            test.get().fail(MarkupHelper.createLabel(message, ExtentColor.RED));
        }
    }
    
    /**
     * Log an exception with stack trace
     * @param throwable Exception to log
     */
    public static void logException(Throwable throwable) {
        if (test.get() != null) {
            test.get().fail(throwable);
        }
    }
    
    /**
     * Add a screenshot to the report
     * @param driver WebDriver instance
     * @param screenshotName Name of the screenshot
     * @return true if screenshot was added successfully, false otherwise
     */
    public static boolean addScreenshot(WebDriver driver, String screenshotName) {
        try {
            String screenshotPath = SCREENSHOT_PATH + screenshotName + "_" + getCurrentDateTime() + ".png";
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(screenshotPath));
            
            // Add screenshot to the report
            if (test.get() != null) {
                test.get().info("Screenshot:", 
                    MediaEntityBuilder.createScreenCaptureFromPath("./screenshots/" + new File(screenshotPath).getName()).build());
            }
            return true;
        } catch (Exception e) {
            logError("Failed to capture screenshot: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * End the current test
     */
    public static void endTest() {
        if (test.get() != null) {
            extent.flush();
        }
    }
    
    /**
     * Flush the report
     */
    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
    
    /**
     * Get current date and time in a formatted string
     * @return Formatted date time string
     */
    private static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
