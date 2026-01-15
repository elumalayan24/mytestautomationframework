package com.myautomation.reports.adapter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestRunFinished;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomExtentCucumberAdapter implements EventListener {
    private static ExtentReports extent;
    private static String testSuiteId;
    private static boolean initialized = false;

    private static String generateTestSuiteId() {
        return "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
               System.currentTimeMillis() % 10000;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        if (!initialized) {
            // Generate test suite ID
            testSuiteId = generateTestSuiteId();
            
            // Print test suite ID to console
            System.out.println("\n===============================================");
            System.out.println("TEST SUITE ID: " + testSuiteId);
            System.out.println("===============================================\n");
            
            // Create ExtentReports instance
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
            
            initialized = true;
        }
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (extent != null) {
            extent.createTest(event.getTestCase().getName());
        }
    }

    private void handleTestRunFinished(TestRunFinished event) {
        if (extent != null) {
            extent.flush();
        }
    }

    public static String getTestSuiteId() {
        return testSuiteId;
    }
}
