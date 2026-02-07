package com.myautomation.database;

import com.myautomation.config.ConfigManager;
import io.cucumber.java.Scenario;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseService {
    
    private static String currentTestSuiteId;
    private static String currentEngine;
    
    public static void initializeTestSuite(String engine) {
        if (!DatabaseConfig.isDatabaseAvailable()) {
            System.out.println("Database not available - test suite will run without database logging");
            currentEngine = engine;
            currentTestSuiteId = generateTestSuiteId();
            return;
        }
        
        currentEngine = engine;
        currentTestSuiteId = generateTestSuiteId();
        
        String sql = """
            INSERT INTO test_suites (test_suite_id, engine, status) 
            VALUES (?, ?, 'RUNNING')
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentTestSuiteId);
            pstmt.setString(2, engine);
            pstmt.executeUpdate();
            
            logInfo("Test Suite Initialized", "Test suite ID: " + currentTestSuiteId + ", Engine: " + engine);
            System.out.println("PostgreSQL: Test suite initialized with ID: " + currentTestSuiteId);
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize test suite in PostgreSQL: " + e.getMessage());
            System.out.println("Continuing without database logging...");
        }
    }
    
    public static void startScenario(Scenario scenario) {
        if (currentTestSuiteId == null) {
            // Detect engine from scenario tags
            String engine = "selenium"; // default
            if (scenario.getSourceTagNames().contains("@playwright")) {
                engine = "playwright";
            } else if (scenario.getSourceTagNames().contains("@mobile")) {
                engine = "mobile";
            }
            initializeTestSuite(engine);
        }
        
        if (!DatabaseConfig.isDatabaseAvailable()) {
            return; // Skip database logging if not available
        }
        
        String sql = """
            INSERT INTO test_scenarios (test_suite_id, scenario_name, feature_file, tags, status, engine) 
            VALUES (?, ?, ?, ?, 'RUNNING', ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentTestSuiteId);
            pstmt.setString(2, scenario.getName());
            pstmt.setString(3, scenario.getUri().toString());
            pstmt.setString(4, String.join(", ", scenario.getSourceTagNames()));
            pstmt.setString(5, currentEngine);
            pstmt.executeUpdate();
            
            logInfo("Scenario Started", "Scenario: " + scenario.getName() + ", Tags: " + scenario.getSourceTagNames());
            
        } catch (SQLException e) {
            System.err.println("Failed to start scenario in PostgreSQL: " + e.getMessage());
        }
    }
    
    public static void finishScenario(Scenario scenario) {
        if (currentTestSuiteId == null || !DatabaseConfig.isDatabaseAvailable()) {
            return; // Skip database logging if not available
        }
        
        String status = scenario.isFailed() ? "FAILED" : "PASSED";
        String errorMessage = scenario.isFailed() ? "Scenario failed - check logs for details" : null;
        
        String sql = """
            UPDATE test_scenarios 
            SET status = ?, end_time = CURRENT_TIMESTAMP, error_message = ?
            WHERE test_suite_id = ? AND scenario_name = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, errorMessage);
            pstmt.setString(3, currentTestSuiteId);
            pstmt.setString(4, scenario.getName());
            pstmt.executeUpdate();
            
            logInfo("Scenario Completed", "Scenario: " + scenario.getName() + ", Status: " + status);
            
            // Update test suite counts
            updateTestSuiteCounts();
            
        } catch (SQLException e) {
            System.err.println("Failed to finish scenario in PostgreSQL: " + e.getMessage());
        }
    }
    
    public static void finishTestSuite() {
        if (currentTestSuiteId == null) return;
        
        String sql = """
            UPDATE test_suites 
            SET end_time = CURRENT_TIMESTAMP, status = 'COMPLETED'
            WHERE test_suite_id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentTestSuiteId);
            pstmt.executeUpdate();
            
            logInfo("Test Suite Completed", "Test suite ID: " + currentTestSuiteId);
            System.out.println("PostgreSQL: Test suite completed: " + currentTestSuiteId);
            
        } catch (SQLException e) {
            System.err.println("Failed to finish test suite in PostgreSQL: " + e.getMessage());
        }
    }
    
    private static void updateTestSuiteCounts() {
        String sql = """
            UPDATE test_suites ts SET 
                total_scenarios = (
                    SELECT COUNT(*) FROM test_scenarios tc 
                    WHERE tc.test_suite_id = ts.test_suite_id
                ),
                passed_scenarios = (
                    SELECT COUNT(*) FROM test_scenarios tc 
                    WHERE tc.test_suite_id = ts.test_suite_id AND tc.status = 'PASSED'
                ),
                failed_scenarios = (
                    SELECT COUNT(*) FROM test_scenarios tc 
                    WHERE tc.test_suite_id = ts.test_suite_id AND tc.status = 'FAILED'
                ),
                skipped_scenarios = (
                    SELECT COUNT(*) FROM test_scenarios tc 
                    WHERE tc.test_suite_id = ts.test_suite_id AND tc.status = 'SKIPPED'
                )
            WHERE test_suite_id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentTestSuiteId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Failed to update test suite counts: " + e.getMessage());
        }
    }
    
    public static void logInfo(String message, String details) {
        log("INFO", message, details);
    }
    
    public static void logError(String message, String details) {
        log("ERROR", message, details);
    }
    
    public static void logWarning(String message, String details) {
        log("WARNING", message, details);
    }
    
    public static void logDebug(String message, String details) {
        log("DEBUG", message, details);
    }
    
    private static void log(String level, String message, String details) {
        if (currentTestSuiteId == null || !DatabaseConfig.isDatabaseAvailable()) {
            return; // Skip database logging if not available
        }
        
        String sql = """
            INSERT INTO test_logs (test_suite_id, log_level, message, engine) 
            VALUES (?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentTestSuiteId);
            pstmt.setString(2, level);
            pstmt.setString(3, message + (details != null ? " | " + details : ""));
            pstmt.setString(4, currentEngine);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Failed to log to PostgreSQL: " + e.getMessage());
        }
    }
    
    private static String generateTestSuiteId() {
        return "TS_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + 
               System.currentTimeMillis() % 10000;
    }
    
    public static String getCurrentTestSuiteId() {
        return currentTestSuiteId;
    }
    
    public static String getCurrentEngine() {
        return currentEngine;
    }
    
    public static void printTestSummary() {
        if (currentTestSuiteId == null) return;
        
        String sql = """
            SELECT test_suite_id, engine, total_scenarios, passed_scenarios, failed_scenarios, 
                   skipped_scenarios, start_time, end_time, status
            FROM test_suites 
            WHERE test_suite_id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentTestSuiteId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n" + "=".repeat(60));
                System.out.println("TEST EXECUTION SUMMARY");
                System.out.println("=".repeat(60));
                System.out.println("Test Suite ID: " + rs.getString("test_suite_id"));
                System.out.println("Engine: " + rs.getString("engine"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Start Time: " + rs.getTimestamp("start_time"));
                System.out.println("End Time: " + rs.getTimestamp("end_time"));
                System.out.println("Total Scenarios: " + rs.getInt("total_scenarios"));
                System.out.println("Passed: " + rs.getInt("passed_scenarios"));
                System.out.println("Failed: " + rs.getInt("failed_scenarios"));
                System.out.println("Skipped: " + rs.getInt("skipped_scenarios"));
                System.out.println("=".repeat(60));
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to print test summary: " + e.getMessage());
        }
    }
}
