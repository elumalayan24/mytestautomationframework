package com.myautomation.reports.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import lombok.Getter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ExtentReportListener implements ITestListener {
    @Getter
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static String testSuiteId;

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void createTest(String name, String description) {
        test.set(extent.createTest(name, description));
    }

    private static String generateTestSuiteId() {
        return "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
               System.currentTimeMillis() % 10000;
    }

    public static String getTestSuiteId() {
        if (testSuiteId == null) {
            testSuiteId = generateTestSuiteId();
        }
        return testSuiteId;
    }
    
    public static synchronized ExtentReports createInstance() {
        if (extent != null) {
            return extent;
        }
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportDir = System.getProperty("user.dir") + File.separator + "test-output";
        String reportPath = reportDir + File.separator + "ExtentReport_" + timeStamp + ".html";
        testSuiteId = generateTestSuiteId();
        
        System.out.println("\n===============================================");
        System.out.println("TEST SUITE ID: " + testSuiteId);
        System.out.println("Report will be generated at: " + reportPath);
        System.out.println("===============================================\n");
        
        // Create test-output directory if it doesn't exist
        File dir = new File(reportDir);
        if (!dir.exists()) {
            boolean dirCreated = dir.mkdirs();
            System.out.println("Report directory created: " + dirCreated + " at " + dir.getAbsolutePath());
        } else {
            System.out.println("Using existing report directory: " + dir.getAbsolutePath());
        }
        
        System.out.println("Report will be generated at: " + reportPath);
        System.out.println("Test Suite ID: " + testSuiteId);
        
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
        
        // Configure the report appearance
        htmlReporter.config().setEncoding("utf-8");
        htmlReporter.config().setDocumentTitle("SauceDemo Test Automation Report");
        htmlReporter.config().setReportName("SauceDemo Test Execution - " + testSuiteId);
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
        
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        
        // Set system information
        extent.setSystemInfo("Test Suite ID", testSuiteId);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("OS Version", System.getProperty("os.version"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("Host Name", getHostName());
        extent.setSystemInfo("User Time Zone", TimeZone.getDefault().getID());
        
        // Add custom information
        extent.setSystemInfo("Report Generated On", new SimpleDateFormat("MMM dd, yyyy hh:mm a z").format(new Date()));
        
        // Add a custom message to the report
        extent.setSystemInfo("Note", "This is an automated test execution report");
        
        return extent;
    }
    
    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    @Override
    public synchronized void onStart(ITestContext context) {
        if (extent == null) {
            createInstance();
        }
    }

    @Override
    public synchronized void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testDescription = result.getMethod().getDescription();
        
        ExtentTest extentTest = extent.createTest(
            testName,
            testDescription != null && !testDescription.isEmpty() ? testDescription : testName
        );
        
        // Add test parameters if any
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            StringBuilder params = new StringBuilder("Test Parameters: ");
            for (Object param : parameters) {
                params.append(param != null ? param.toString() : "null").append(", ");
            }
            extentTest.info(params.toString().replaceAll(", $", ""));
        }
        
        test.set(extentTest);
    }

    @Override
    public synchronized void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, MarkupHelper.createLabel("Test Passed", ExtentColor.GREEN));
        test.get().log(Status.INFO, "Test completed successfully at " + new Date(result.getEndMillis()));
    }

    @Override
    public synchronized void onTestFailure(ITestResult result) {
        Throwable throwable = result.getThrowable();
        String errorMsg = throwable != null ? throwable.getMessage() : "Unknown error";
        
        test.get().fail(MarkupHelper.createLabel("Test Failed: " + errorMsg, ExtentColor.RED));
        
        if (throwable != null) {
            test.get().fail(throwable);
            
            // Add screenshot if available
            if (result.getAttribute("screenshot") != null) {
                test.get().addScreenCaptureFromPath(result.getAttribute("screenshot").toString());
            }
        }
        
        test.get().log(Status.INFO, "Test failed at " + new Date(result.getEndMillis()));
    }

    @Override
    public synchronized void onTestSkipped(ITestResult result) {
        Throwable throwable = result.getThrowable();
        String skipMsg = throwable != null ? throwable.getMessage() : "Test was skipped";
        
        test.get().skip(MarkupHelper.createLabel("Test Skipped: " + skipMsg, ExtentColor.ORANGE));
        
        if (throwable != null) {
            test.get().skip(throwable);
        }
        
        test.get().log(Status.INFO, "Test was skipped at " + new Date(result.getEndMillis()));
    }

    @Override
    public synchronized void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
