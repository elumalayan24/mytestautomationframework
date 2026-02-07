package com.myautomation.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FailureReasonService {
    
    public static void logFailureReason(String testSuiteId, String scenarioName, 
                                       String failureType, String errorMessage, 
                                       String stackTrace, String screenshotPath,
                                       String elementLocator, String expectedValue, 
                                       String actualValue, String pageUrl, 
                                       String browserInfo, String engine) {
        
        String sql = """
            INSERT INTO test_failure_reasons (
                test_suite_id, scenario_name, failure_type, error_message, 
                stack_trace, screenshot_path, element_locator, expected_value, 
                actual_value, page_url, browser_info, engine
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, testSuiteId);
            pstmt.setString(2, scenarioName);
            pstmt.setString(3, failureType);
            pstmt.setString(4, errorMessage);
            pstmt.setString(5, stackTrace);
            pstmt.setString(6, screenshotPath);
            pstmt.setString(7, elementLocator);
            pstmt.setString(8, expectedValue);
            pstmt.setString(9, actualValue);
            pstmt.setString(10, pageUrl);
            pstmt.setString(11, browserInfo);
            pstmt.setString(12, engine);
            
            pstmt.executeUpdate();
            System.out.println("🔴 Failure reason logged: " + failureType + " for " + scenarioName);
            
        } catch (SQLException e) {
            System.err.println("Failed to log failure reason: " + e.getMessage());
        }
    }
    
    public static void logAssertionFailure(String testSuiteId, String scenarioName, 
                                         String errorMessage, String expectedValue, 
                                         String actualValue, String engine) {
        logFailureReason(testSuiteId, scenarioName, "ASSERTION_FAILURE", 
                        errorMessage, null, null, null, 
                        expectedValue, actualValue, null, null, engine);
    }
    
    public static void logElementNotFound(String testSuiteId, String scenarioName, 
                                        String elementLocator, String pageUrl, 
                                        String engine) {
        logFailureReason(testSuiteId, scenarioName, "ELEMENT_NOT_FOUND", 
                        "Element not found: " + elementLocator, null, null, 
                        elementLocator, null, null, pageUrl, null, engine);
    }
    
    public static void logTimeoutFailure(String testSuiteId, String scenarioName, 
                                       String errorMessage, long timeoutMs, 
                                       String engine) {
        logFailureReason(testSuiteId, scenarioName, "TIMEOUT_FAILURE", 
                        errorMessage, null, null, null, 
                        "Operation to complete within " + timeoutMs + "ms", 
                        "Operation timed out", null, null, engine);
    }
    
    public static void logNavigationFailure(String testSuiteId, String scenarioName, 
                                           String targetUrl, String errorMessage, 
                                           String engine) {
        logFailureReason(testSuiteId, scenarioName, "NAVIGATION_FAILURE", 
                        errorMessage, null, null, null, 
                        targetUrl, "Failed to navigate", null, null, engine);
    }
    
    public static void logJavaScriptError(String testSuiteId, String scenarioName, 
                                         String errorMessage, String pageUrl, 
                                         String engine) {
        logFailureReason(testSuiteId, scenarioName, "JAVASCRIPT_ERROR", 
                        errorMessage, null, null, null, 
                        "JavaScript execution successful", 
                        "JavaScript error occurred", pageUrl, null, engine);
    }
    
    public static void getFailureAnalysis(String testSuiteId) {
        String sql = """
            SELECT failure_type, COUNT(*) as count, 
                   STRING_AGG(DISTINCT scenario_name, ', ') as affected_scenarios
            FROM test_failure_reasons 
            WHERE test_suite_id = ?
            GROUP BY failure_type 
            ORDER BY count DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, testSuiteId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n🔴 FAILURE ANALYSIS FOR: " + testSuiteId);
            System.out.println("=" .repeat(60));
            
            while (rs.next()) {
                System.out.printf("%-20s: %d failures (Scenarios: %s)%n",
                    rs.getString("failure_type"),
                    rs.getInt("count"),
                    rs.getString("affected_scenarios"));
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get failure analysis: " + e.getMessage());
        }
    }
    
    public static void getTopFailureReasons(int limit) {
        String sql = """
            SELECT failure_type, COUNT(*) as total_failures,
                   COUNT(DISTINCT test_suite_id) as affected_suites,
                   COUNT(DISTINCT scenario_name) as affected_scenarios
            FROM test_failure_reasons 
            GROUP BY failure_type 
            ORDER BY total_failures DESC 
            LIMIT ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n🔴 TOP " + limit + " FAILURE REASONS");
            System.out.println("=" .repeat(80));
            System.out.printf("%-20s %-10s %-15s %-15s%n", 
                "Failure Type", "Total", "Suites", "Scenarios");
            System.out.println("-".repeat(80));
            
            while (rs.next()) {
                System.out.printf("%-20s %-10d %-15d %-15d%n",
                    rs.getString("failure_type"),
                    rs.getInt("total_failures"),
                    rs.getInt("affected_suites"),
                    rs.getInt("affected_scenarios"));
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get top failure reasons: " + e.getMessage());
        }
    }
}
