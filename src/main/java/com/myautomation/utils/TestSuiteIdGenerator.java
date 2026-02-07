package com.myautomation.utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TestSuiteIdGenerator {
    private static String testSuiteId;

    public static synchronized String getTestSuiteId() {
        if (testSuiteId == null) {
            testSuiteId = "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
                    + "_" + System.currentTimeMillis() % 10000;
            System.setProperty("test.suite.id", testSuiteId);
            System.out.println("TEST SUITE ID: " + testSuiteId);
        }
        return testSuiteId;
    }
}
