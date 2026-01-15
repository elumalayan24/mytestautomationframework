package com.myautomation.plugins;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunStarted;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestSuiteIdPlugin implements EventListener {
    private static boolean testSuiteIdGenerated = false;
    private static String testSuiteId;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        if (!testSuiteIdGenerated) {
            testSuiteId = generateTestSuiteId();
            testSuiteIdGenerated = true;
            
            // Print test suite ID to console
            System.out.println("TEST SUITE ID: " + testSuiteId);
            
            // Set system property so other components can access it
            System.setProperty("test.suite.id", testSuiteId);
        }
    }

    private String generateTestSuiteId() {
        return "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
               System.currentTimeMillis() % 10000;
    }

    public static String getTestSuiteId() {
        if (testSuiteId == null) {
            // Fallback if plugin didn't run
            testSuiteId = "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
                          System.currentTimeMillis() % 10000;
        }
        return testSuiteId;
    }
}
