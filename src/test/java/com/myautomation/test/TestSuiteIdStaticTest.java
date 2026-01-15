package com.myautomation.test;

import com.myautomation.stepdefinitions.LoginSteps;
import com.myautomation.utils.TestSuiteIdGenerator;

public class TestSuiteIdStaticTest {
    public static void main(String[] args) {
        System.out.println("Testing static initializer approach...");
        
        // This should trigger the static initializer in LoginSteps
        Class<?> loginStepsClass = LoginSteps.class;
        System.out.println("LoginSteps class loaded: " + loginStepsClass.getName());
        
        // Get the test suite ID
        String testSuiteId = TestSuiteIdGenerator.getTestSuiteId();
        System.out.println("Test Suite ID: " + testSuiteId);
    }
}
