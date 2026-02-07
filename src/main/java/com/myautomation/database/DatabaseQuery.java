package com.myautomation.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseQuery {
    
    public static void main(String[] args) {
        System.out.println("=== Test Results Query ===\n");
        
        try {
            // Test database connection
            if (!DatabaseConfig.isDatabaseAvailable()) {
                System.out.println("❌ Database is not available");
                return;
            }
            
            System.out.println("✅ Database connected successfully\n");
            
            // Query recent test suites
            queryRecentTestSuites();
            
            // Query recent scenarios
            queryRecentScenarios();
            
            // Query recent logs
            queryRecentLogs();
            
        } catch (Exception e) {
            System.err.println("Error querying database: " + e.getMessage());
        }
    }
    
    private static void queryRecentTestSuites() {
        System.out.println("📊 RECENT TEST SUITES:");
        System.out.println("=" .repeat(80));
        
        String sql = """
            SELECT test_suite_id, engine, start_time, end_time, status, 
                   total_scenarios, passed_scenarios, failed_scenarios, skipped_scenarios
            FROM test_suites 
            ORDER BY start_time DESC 
            LIMIT 5
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                System.out.println("Test Suite ID: " + rs.getString("test_suite_id"));
                System.out.println("Engine: " + rs.getString("engine"));
                System.out.println("Start Time: " + rs.getTimestamp("start_time"));
                System.out.println("End Time: " + rs.getTimestamp("end_time"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Scenarios - Total: " + rs.getInt("total_scenarios") + 
                                 ", Passed: " + rs.getInt("passed_scenarios") + 
                                 ", Failed: " + rs.getInt("failed_scenarios") + 
                                 ", Skipped: " + rs.getInt("skipped_scenarios"));
                System.out.println("-".repeat(40));
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying test suites: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void queryRecentScenarios() {
        System.out.println("📋 RECENT SCENARIOS:");
        System.out.println("=" .repeat(80));
        
        String sql = """
            SELECT test_suite_id, scenario_name, feature_file, tags, 
                   start_time, end_time, status, error_message, engine
            FROM test_scenarios 
            ORDER BY start_time DESC 
            LIMIT 10
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                System.out.println("Scenario: " + rs.getString("scenario_name"));
                System.out.println("Test Suite: " + rs.getString("test_suite_id"));
                System.out.println("Engine: " + rs.getString("engine"));
                System.out.println("Feature: " + rs.getString("feature_file"));
                System.out.println("Tags: " + rs.getString("tags"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Start: " + rs.getTimestamp("start_time"));
                if (rs.getTimestamp("end_time") != null) {
                    System.out.println("End: " + rs.getTimestamp("end_time"));
                }
                if (rs.getString("error_message") != null) {
                    System.out.println("Error: " + rs.getString("error_message"));
                }
                System.out.println("-".repeat(40));
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying scenarios: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void queryRecentLogs() {
        System.out.println("📝 RECENT LOGS:");
        System.out.println("=" .repeat(80));
        
        String sql = """
            SELECT test_suite_id, scenario_name, log_level, message, timestamp, engine
            FROM test_logs 
            ORDER BY timestamp DESC 
            LIMIT 15
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                System.out.println("[" + rs.getTimestamp("timestamp") + "] " + 
                                 rs.getString("log_level").toUpperCase() + 
                                 " - " + rs.getString("message"));
                System.out.println("   Test Suite: " + rs.getString("test_suite_id"));
                if (rs.getString("scenario_name") != null) {
                    System.out.println("   Scenario: " + rs.getString("scenario_name"));
                }
                System.out.println("   Engine: " + rs.getString("engine"));
                System.out.println();
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying logs: " + e.getMessage());
        }
    }
}
