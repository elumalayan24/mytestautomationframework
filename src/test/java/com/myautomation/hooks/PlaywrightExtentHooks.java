package com.myautomation.hooks;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;
import com.myautomation.core.drivers.PlaywrightDriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import static com.myautomation.utils.LogCaptureUtil.log;
import com.myautomation.utils.LogCaptureUtil;
import com.myautomation.utils.PerformanceMonitor;

public class PlaywrightExtentHooks {
    private static boolean suiteInitialized = false;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @Before(order = 0, value = "@playwright")
    public void beforeAll(Scenario scenario) {
        if (!suiteInitialized) {
            log("Starting Playwright test suite");
            suiteInitialized = true;
        }
    }

    @Before(order = 1, value = "@playwright")
    public void beforeScenario(Scenario scenario) {
        PerformanceMonitor.startTimer("scenario_" + scenario.getName());
        LogCaptureUtil.startCapture();
        log("Starting Playwright scenario: " + scenario.getName());
        // Playwright driver is initialized lazily in step definitions
    }

    @After(order = 1, value = "@playwright")
    public void afterScenario(Scenario scenario) {
        try {
            PerformanceMonitor.endTimer("scenario_" + scenario.getName());
            PerformanceMonitor.incrementCounter(scenario.isFailed() ? "failed_scenarios" : "passed_scenarios");
            if (scenario.isFailed()) {
                log("Playwright SCENARIO FAILED: " + scenario.getName());
                if (extentTest.get() != null) {
                    extentTest.get().log(Status.FAIL, "Scenario Failed: " + scenario.getName());
                }
            } else {
                log("Playwright SCENARIO PASSED: " + scenario.getName());
                if (extentTest.get() != null) {
                    extentTest.get().log(Status.PASS, "Scenario Passed: " + scenario.getName());
                }
            }

            // Add logs to ExtentReports
            if (extentTest.get() != null) {
                LogCaptureUtil.addLogsToReport(extentTest.get());
            }

            // Take screenshot on failure
            if (scenario.isFailed()) {
                Page page = PlaywrightDriverManager.getDriver();
                if (page != null) {
                    try {
                        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                                .setFullPage(true));
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String screenshotName = String.format("playwright_screenshot_failed_%s_%s.png",
                                scenario.getName().replaceAll("\\s+", "_"),
                                timestamp
                        );
                        
                        // Save screenshot to file system
                        PerformanceMonitor.startTimer("screenshot_capture");
                        String screenshotsDir = "test-output/screenshots";
                        java.io.File dir = new java.io.File(screenshotsDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String screenshotPath = screenshotsDir + "/" + screenshotName;
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(screenshotPath);
                        fos.write(screenshot);
                        fos.close();
                        PerformanceMonitor.endTimer("screenshot_capture");
                        
                        scenario.attach(screenshot, "image/png", screenshotName);
                        log("Saved Playwright screenshot to: " + screenshotPath);
                        
                        if (extentTest.get() != null) {
                            extentTest.get().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
                        }
                    } catch (Exception e) {
                        log("Failed to capture Playwright screenshot: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log("Error in Playwright afterScenario: " + e.getMessage());
        } finally {
            // Clean up page and context after each scenario, but keep browser alive
            log("Cleaning up Playwright resources after scenario: " + scenario.getName());
            PlaywrightDriverManager.closePage();
            PlaywrightDriverManager.closeContext();
            LogCaptureUtil.stopCapture();
            extentTest.remove();
        }
    }

    @After(order = 1000, value = "@playwright")
    public void afterAll(Scenario scenario) {
        try {
            log("=== Final Playwright cleanup after all test scenarios ===");
            
            // Ensure any remaining Playwright instances are closed
            PlaywrightDriverManager.unload();
            PlaywrightDriverManager.shutdownBrowserPool();
            
            // Print performance report
            PerformanceMonitor.printReport();
            
            log("=== All Playwright cleanup completed ===");
        } catch (Exception e) {
            log("Error in Playwright afterAll cleanup: " + e.getMessage());
        }
    }

    // Method to set the ExtentTest instance (called by ExtentCucumberAdapter)
    public static void setExtentTest(ExtentTest test) {
        extentTest.set(test);
    }
}
