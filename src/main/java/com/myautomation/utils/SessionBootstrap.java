package com.myautomation.utils;

public class SessionBootstrap {

    static {
        String suiteId = TestSuiteIdGenerator.getTestSuiteId();
        System.out.println("🚀 Test Suite Started: " + suiteId);
    }
}
