package com.myautomation.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentReportListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static ExtentTest getTest() {
        return test.get();
    }
    
    public static ExtentReports getExtent() {
        return extent;
    }
    
    public static void createTest(String name, String description) {
        test.set(extent.createTest(name, description));
    }

    public static synchronized ExtentReports createInstance() {
        if (extent != null) {
            return extent;
        }
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportDir = System.getProperty("user.dir") + "/test-output";
        String reportPath = reportDir + "/ExtentReport_" + timeStamp + ".html";
        
        // Create test-output directory if it doesn't exist
        File dir = new File(reportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
        htmlReporter.config().setDocumentTitle("SauceDemo Test Automation Report");
        htmlReporter.config().setReportName("SauceDemo Test Execution Report");
        htmlReporter.config().setTheme(Theme.STANDARD);
        
        // Optional: Configure the report look and feel
        htmlReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
        
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        
        // Set system information
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("OS Version", System.getProperty("os.version"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("Environment", "QA");
        
        return extent;
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
