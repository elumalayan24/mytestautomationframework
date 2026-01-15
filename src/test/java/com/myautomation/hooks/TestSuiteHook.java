package com.myautomation.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

public class TestSuiteHook {
    
    @Before(order = 1) // Changed order to 1 to run after CucumberReportHooks
    public void beforeAll(Scenario scenario) {
        System.out.println("[DEBUG] TestSuiteHook.beforeAll() called - delegating to CucumberReportHooks");
    }
    
    @After(order = 1000)
    public void afterAll(Scenario scenario) {
        System.out.println("[DEBUG] TestSuiteHook.afterAll() called");
    }
}
