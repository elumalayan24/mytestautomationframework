package com.myautomation.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestSuiteIdGenerator {
    private static String testSuiteId;
    private static boolean initialized = false;
    
    public static String generateTestSuiteId() {
        return "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
               System.currentTimeMillis() % 10000;
    }
    
    public static String getTestSuiteId() {
        if (!initialized) {
            testSuiteId = generateTestSuiteId();
            initialized = true;
            
            // Print test suite ID to console with prominent formatting
            System.out.println("");
            System.out.println("********************************************************************************");
            System.out.println("***                          TEST SUITE ID: " + testSuiteId + "                          ***");
            System.out.println("********************************************************************************");
            System.out.println("");
        }
        return testSuiteId;
    }
}
