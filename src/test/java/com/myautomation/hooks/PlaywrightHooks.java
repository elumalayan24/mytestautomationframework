package com.myautomation.hooks;

import com.microsoft.playwright.Page;
import com.myautomation.core.drivers.PlaywrightDriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import static com.myautomation.utils.LogCaptureUtil.log;

public class PlaywrightHooks {
    private static boolean suiteInitialized = false;

    @Before(order = 0)
    public void beforeAll(Scenario scenario) {
        if (!suiteInitialized) {
            log("Starting Playwright test suite");
            suiteInitialized = true;
        }
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        log("Starting Playwright scenario: " + scenario.getName());
        // Playwright driver is initialized lazily in step definitions
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                log("Playwright SCENARIO FAILED: " + scenario.getName());
            } else {
                log("Playwright SCENARIO PASSED: " + scenario.getName());
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
                        scenario.attach(screenshot, "image/png", screenshotName);
                        log("Attached Playwright screenshot for failed scenario");
                    } catch (Exception e) {
                        log("Failed to capture Playwright screenshot: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log("Error in Playwright afterScenario: " + e.getMessage());
        } finally {
            // Clean up Playwright resources after each scenario
            log("Cleaning up Playwright resources after scenario: " + scenario.getName());
            PlaywrightDriverManager.unload();
        }
    }

    @After(order = 1000)
    public void afterAll(Scenario scenario) {
        try {
            log("=== Final Playwright cleanup after all test scenarios ===");
            
            // Ensure any remaining Playwright instances are closed
            PlaywrightDriverManager.unload();
            
            log("=== All Playwright cleanup completed ===");
        } catch (Exception e) {
            log("Error in Playwright afterAll cleanup: " + e.getMessage());
        }
    }
}
