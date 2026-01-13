package com.myautomation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.myautomation.utils.DriverManager;

import java.util.UUID;

/**
 * Hooks for Cucumber test execution with enhanced reporting and screenshot capabilities.
 */
public class CucumberReportHooks {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<String> scenarioName = new ThreadLocal<>();

    @Before
    public void beforeScenario(Scenario scenario) {
        scenarioName.set(scenario.getName());
        System.out.println("\n\n=== Starting Scenario: " + scenario.getName() + " ===\n");
        
        // Initialize WebDriver if not already done
        if (driver.get() == null) {
            driver.set(DriverManager.getDriver());
        }
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        // Take screenshot on step failure
        if (scenario.isFailed()) {
            WebDriver currentDriver = driver.get();
            if (currentDriver != null) {
                try {
                    byte[] screenshot = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "screenshot_" +
                        scenario.getName().replaceAll("\\s+", "_") + "_" +
                        UUID.randomUUID().toString().substring(0, 6) + ".png");

                    // Log the failure with step details
                    System.out.println("\n[FAILED] Step failed at: " + scenario.getLine() +
                                     "\nError details: " + scenario.getStatus().name() +
                                     "\nScenario: " + scenario.getName());
                } catch (Exception e) {
                    System.err.println("Failed to capture screenshot: " + e.getMessage());
                }
            }
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        // Take final screenshot if scenario failed
        if (scenario.isFailed()) {
            WebDriver currentDriver = driver.get();
            if (currentDriver != null) {
                try {
                    byte[] screenshot = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "failure_screenshot_" +
                        scenario.getName().replaceAll("\\s+", "_") + ".png");
                } catch (Exception e) {
                    System.err.println("Failed to capture final screenshot: " + e.getMessage());
                }
            }

            System.out.println("\n\n=== SCENARIO FAILED: " + scenario.getName() + " ===");
            System.out.println("Status: " + scenario.getStatus().name());
            System.out.println("URI: " + scenario.getUri());
            System.out.println("Line: " + scenario.getLine() + "\n");
        } else {
            System.out.println("\n\n=== SCENARIO PASSED: " + scenario.getName() + " ===\n");
        }
        
        // Close the WebDriver after scenario
        cleanupDriver();
    }
    
    private void cleanupDriver() {
        WebDriver currentDriver = driver.get();
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } catch (Exception e) {
                System.err.println("Error while closing WebDriver: " + e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
}
