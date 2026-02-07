package com.myautomation.hooks;

import io.cucumber.java.Status;
import com.myautomation.core.drivers.DriverFactory;
import com.myautomation.reporting.ExtentManager;
import com.aventstack.extentreports.ExtentTest;
import io.cucumber.java.AfterStep;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class SeleniumHooks {

    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Before("@selenium")
    public void beforeScenario(Scenario scenario) {
        test.set(ExtentManager.getInstance().createTest(scenario.getName()));
        test.get().info("Starting Selenium Scenario: " + scenario.getName());
    }

    @AfterStep("@selenium")
    public void afterStep(Scenario scenario) {
        try {
            TakesScreenshot ts = (TakesScreenshot) DriverFactory.getDriver();
            byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Step Screenshot");
            test.get().addScreenCaptureFromBase64String(java.util.Base64.getEncoder().encodeToString(screenshot));
        } catch (Exception e) {
            test.get().warning("Failed to capture screenshot: " + e.getMessage());
        }
    }

    @After("@selenium")
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            test.get().fail("Scenario Failed");
        } else if (scenario.getStatus() == Status.SKIPPED) {
            test.get().skip("Scenario Skipped");
        } else {
            test.get().pass("Scenario Passed");
        }
    }
}

