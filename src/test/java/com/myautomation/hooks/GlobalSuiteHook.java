package com.myautomation.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import com.myautomation.reporting.ExtentManager;
import com.myautomation.utils.TestSuiteIdGenerator;

public class GlobalSuiteHook {

    @Before(order = 0)
    public void beforeSuite(Scenario scenario) {
        TestSuiteIdGenerator.getTestSuiteId();
        ExtentManager.getInstance();
    }

    @After(order = 1000)
    public void afterSuite() {
        ExtentManager.getInstance().flush();
        System.out.println("SUITE FINISHED");
    }
}
