package com.myautomation.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.myautomation.utils.TestSuiteIdGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ExtentManager {

    private static ExtentReports extent;

    private ExtentManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String suiteId = TestSuiteIdGenerator.getTestSuiteId();
            String reportPath = "test-output/reports/ExtentReport_" + suiteId + ".html";

            try {
                // Ensure report directory exists
                File reportDir = new File("test-output/reports");
                if (!reportDir.exists()) {
                    reportDir.mkdirs();
                }

                ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
                
                // Configure screenshot directory and report settings
                spark.config().setReportName("Test Automation Report");
                spark.config().setDocumentTitle("Automation Test Report");
                spark.config().setEncoding("utf-8");
                spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
                
                // Set the screenshot directory for relative path resolution
                spark.config().setCss("img { max-width: 100%; height: auto; }");
                
                // Apply custom styling and functionality
                ReportCustomizer.inject(spark);
                
                                
                // Load XML config with proper error handling
                String configPath = "src/test/resources/extent-config.xml";
                if (Files.exists(Paths.get(configPath))) {
                    spark.loadXMLConfig(configPath);
                } else {
                    System.err.println("Warning: Extent config file not found at " + configPath + 
                                     ". Using default configuration.");
                }

                extent = new ExtentReports();
                extent.attachReporter(spark);

                extent.setSystemInfo("Suite ID", suiteId);
                extent.setSystemInfo("OS", System.getProperty("os.name"));
                extent.setSystemInfo("Java", System.getProperty("java.version"));
            } catch (IOException e) {
                System.err.println("Error initializing ExtentReports: " + e.getMessage());
                // Fallback to basic configuration without XML
                ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
                
                // Configure screenshot directory and report settings
                spark.config().setReportName("Test Automation Report");
                spark.config().setDocumentTitle("Automation Test Report");
                spark.config().setEncoding("utf-8");
                spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
                
                // Set the screenshot directory for relative path resolution
                spark.config().setCss("img { max-width: 100%; height: auto; }");
                
                // Apply custom styling and functionality even in fallback mode
                ReportCustomizer.inject(spark);
                
                                
                extent = new ExtentReports();
                extent.attachReporter(spark);
                extent.setSystemInfo("Suite ID", suiteId);
                extent.setSystemInfo("OS", System.getProperty("os.name"));
                extent.setSystemInfo("Java", System.getProperty("java.version"));
            }
        }
        return extent;
    }
}
