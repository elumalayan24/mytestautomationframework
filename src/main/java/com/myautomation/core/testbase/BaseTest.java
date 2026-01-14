package com.myautomation.core.testbase;

import com.myautomation.core.drivers.DriverManager;
import com.myautomation.utils.ReporterClass;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base test class that provides common setup and teardown methods for all tests.
 */
@Listeners(com.myautomation.reports.listeners.ExtentReportListener.class)
public abstract class BaseTest {
    protected WebDriver driver;
    protected ReporterClass reporter;

    @BeforeMethod
    public void setUp() {
        // Initialize WebDriver
        driver = DriverManager.getDriver();
        
        // Initialize reporter
        reporter = new ReporterClass();
        
        // Additional setup can be added here
    }

    @AfterMethod
    public void tearDown() {
        // Quit the WebDriver instance
        if (driver != null) {
            driver.quit();
            DriverManager.unload();
        }
        
        // Flush the report
        if (reporter != null) {
            reporter.flushReport();
        }
    }

    // Common test utilities can be added here
}
