package com.myautomation.hooks;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.myautomation.utils.LogCaptureUtil;
import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.myautomation.utils.DriverManager;

public class CucumberReportHooks {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static ExtentReports extent;

    @Before
    public void beforeScenario(Scenario scenario) {
        LogCaptureUtil.startCapture();
        LogCaptureUtil.log("Starting scenario: " + scenario.getName());

        if (driver.get() == null) {
            driver.set(DriverManager.getDriver());
        }

        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("Test Execution Report");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }

        ExtentTest test = extent.createTest(scenario.getName());
        extentTest.set(test);
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        WebDriver currentDriver = driver.get();
        if (currentDriver != null) {
            try {
                byte[] screenshot = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.BYTES);
                String status = scenario.isFailed() ? "FAILED" : "PASSED";
                String timestamp = String.valueOf(System.currentTimeMillis());
                String screenshotName = String.format("screenshot_%s_%s_%s.png",
                        status.toLowerCase(),
                        scenario.getName().replaceAll("\\s+", "_"),
                        timestamp
                );

                scenario.attach(screenshot, "image/png", screenshotName);
                LogCaptureUtil.addLogsToReport(extentTest.get());

                if (scenario.isFailed()) {
                    extentTest.get().log(Status.FAIL, "Step Failed");
                    LogCaptureUtil.log("Step failed: " + scenario.getStatus().name());
                } else {
                    extentTest.get().log(Status.PASS, "Step Passed");
                }

            } catch (Exception e) {
                LogCaptureUtil.log("Error in afterStep: " + e.getMessage());
                extentTest.get().log(Status.WARNING, "Failed to capture step details");
            }
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                LogCaptureUtil.log("SCENARIO FAILED: " + scenario.getName());
                extentTest.get().log(Status.FAIL, "Scenario Failed");
            } else {
                LogCaptureUtil.log("SCENARIO PASSED: " + scenario.getName());
                extentTest.get().log(Status.PASS, "Scenario Passed");
            }

            LogCaptureUtil.addLogsToReport(extentTest.get());

        } catch (Exception e) {
            LogCaptureUtil.log("Error in afterScenario: " + e.getMessage());
        } finally {
            cleanupDriver();
            LogCaptureUtil.stopCapture();

            if (extent != null) {
                extent.flush();
            }
        }
    }

    private void cleanupDriver() {
        WebDriver currentDriver = driver.get();
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } catch (Exception e) {
                LogCaptureUtil.log("Error while closing WebDriver: " + e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
}