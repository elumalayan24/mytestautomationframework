package com.myautomation.tests;

import com.aventstack.extentreports.ExtentTest;
import com.myautomation.listeners.ExtentReportListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

public class BaseTest {
    protected ExtentTest test;

    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        test = ExtentReportListener.getTest();
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        // Add any cleanup code here if needed
    }
}
