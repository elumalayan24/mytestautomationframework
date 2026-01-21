package com.myautomation.adapters;

import com.aventstack.extentreports.ExtentTest;
import com.myautomation.utils.LogCaptureUtil;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

import java.util.HashMap;
import java.util.Map;

public class CustomExtentCucumberAdapter implements EventListener {
    private static final Map<String, ExtentTest> scenarioTests = new HashMap<>();
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, handleTestCaseStarted());
        publisher.registerHandlerFor(TestCaseFinished.class, handleTestCaseFinished());
        publisher.registerHandlerFor(TestStepFinished.class, handleTestStepFinished());
    }

    private EventHandler<TestCaseStarted> handleTestCaseStarted() {
        return event -> {
            String scenarioName = event.getTestCase().getName();
            // Create or get the ExtentTest for this scenario
            // This will be managed by the main ExtentCucumberAdapter
            LogCaptureUtil.startCapture();
            LogCaptureUtil.log("Starting scenario: " + scenarioName);
        };
    }

    private EventHandler<TestCaseFinished> handleTestCaseFinished() {
        return event -> {
            String scenarioName = event.getTestCase().getName();
            Result result = event.getResult();
            
            if (result.getStatus() == io.cucumber.plugin.event.Status.FAILED) {
                LogCaptureUtil.log("Scenario FAILED: " + scenarioName);
            } else {
                LogCaptureUtil.log("Scenario PASSED: " + scenarioName);
            }
            
            // Add logs to the current test if available
            ExtentTest test = currentTest.get();
            if (test != null) {
                LogCaptureUtil.addLogsToReport(test);
            }
            
            LogCaptureUtil.stopCapture();
            currentTest.remove();
        };
    }

    private EventHandler<TestStepFinished> handleTestStepFinished() {
        return event -> {
            if (event.getTestStep() instanceof PickleStepTestStep) {
                PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
                Result result = event.getResult();
                
                String stepText = step.getStepText();
                if (result.getStatus() == io.cucumber.plugin.event.Status.FAILED) {
                    LogCaptureUtil.log("Step FAILED: " + stepText);
                    if (result.getError() != null) {
                        LogCaptureUtil.log("Error: " + result.getError().getMessage());
                    }
                } else if (result.getStatus() == io.cucumber.plugin.event.Status.PASSED) {
                    LogCaptureUtil.log("Step PASSED: " + stepText);
                }
                
                // Add logs to current test after each step
                ExtentTest test = currentTest.get();
                if (test != null) {
                    LogCaptureUtil.addLogsToReport(test);
                }
            }
        };
    }

    // Method to set the current ExtentTest (called by main adapter)
    public static void setCurrentTest(ExtentTest test) {
        currentTest.set(test);
    }
}
