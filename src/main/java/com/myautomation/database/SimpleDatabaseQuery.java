package com.myautomation.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDatabaseQuery {
    
    public static void main(String[] args) {
        System.out.println("=== Test Results Query ===\n");
        
        // Direct database connection
        String url = "jdbc:postgresql://localhost:5432/test_automation";
        String username = "postgres";
        String password = "admin";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ Database connected successfully\n");
            
            // Query recent test suites
            queryRecentTestSuites(conn);
            
            // Query recent scenarios
            queryRecentScenarios(conn);
            
            // Query recent logs
            queryRecentLogs(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Error connecting to database: " + e.getMessage());
            if (e.getMessage().contains("password")) {
                System.err.println("💡 Check your PostgreSQL password in database.properties");
            }
        }
    }
    
    private static void queryRecentTestSuites(Connection conn) {
        System.out.println("📊 RECENT TEST SUITES:");
        System.out.println("=" .repeat(80));
        
        String sql = """
            SELECT test_suite_id, engine, start_time, end_time, status, 
                   total_scenarios, passed_scenarios, failed_scenarios, skipped_scenarios
            FROM test_suites 
            ORDER BY start_time DESC 
            LIMIT 5
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("🔹 Test Suite ID: " + rs.getString("test_suite_id"));
                System.out.println("   Engine: " + rs.getString("engine"));
                System.out.println("   Start Time: " + rs.getTimestamp("start_time"));
                System.out.println("   End Time: " + rs.getTimestamp("end_time"));
                System.out.println("   Status: " + rs.getString("status"));
                System.out.println("   Scenarios - Total: " + rs.getInt("total_scenarios") + 
                                 ", Passed: " + rs.getInt("passed_scenarios") + 
                                 ", Failed: " + rs.getInt("failed_scenarios") + 
                                 ", Skipped: " + rs.getInt("skipped_scenarios"));
                System.out.println("-".repeat(50));
            }
            
            if (!found) {
                System.out.println("No test suites found in database");
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying test suites: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void queryRecentScenarios(Connection conn) {
        System.out.println("📋 RECENT SCENARIOS:");
        System.out.println("=" .repeat(80));
        
        String sql = """
            SELECT test_suite_id, scenario_name, feature_file, tags, 
                   start_time, end_time, status, error_message, engine
            FROM test_scenarios 
            ORDER BY start_time DESC 
            LIMIT 10
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("🔹 Scenario: " + rs.getString("scenario_name"));
                System.out.println("   Test Suite: " + rs.getString("test_suite_id"));
                System.out.println("   Engine: " + rs.getString("engine"));
                System.out.println("   Feature: " + rs.getString("feature_file"));
                System.out.println("   Tags: " + rs.getString("tags"));
                System.out.println("   Status: " + rs.getString("status"));
                System.out.println("   Start: " + rs.getTimestamp("start_time"));
                if (rs.getTimestamp("end_time") != null) {
                    System.out.println("   End: " + rs.getTimestamp("end_time"));
                }
                if (rs.getString("error_message") != null) {
                    System.out.println("   Error: " + rs.getString("error_message"));
                }
                System.out.println("-".repeat(50));
            }
            
            if (!found) {
                System.out.println("No scenarios found in database");
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying scenarios: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void queryRecentLogs(Connection conn) {
        System.out.println("📝 RECENT LOGS:");
        System.out.println("=" .repeat(80));
        
        String sql = """
            SELECT test_suite_id, scenario_name, log_level, message, timestamp, engine
            FROM test_logs 
            ORDER BY timestamp DESC 
            LIMIT 15
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("🔹 [" + rs.getTimestamp("timestamp") + "] " + 
                                 rs.getString("log_level").toUpperCase() + 
                                 " - " + rs.getString("message"));
                System.out.println("   Test Suite: " + rs.getString("test_suite_id"));
                if (rs.getString("scenario_name") != null) {
                    System.out.println("   Scenario: " + rs.getString("scenario_name"));
                }
                System.out.println("   Engine: " + rs.getString("engine"));
                System.out.println();
            }
            
            if (!found) {
                System.out.println("No logs found in database");
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying logs: " + e.getMessage());
        }
    }
}
