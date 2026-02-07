package com.myautomation.database;

import java.sql.*;

public class DatabaseQueryTool {
    
    public static void main(String[] args) {
        System.out.println("=== PostgreSQL Database Query Tool ===\n");
        
        // Load PostgreSQL driver
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL driver loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL driver not found: " + e.getMessage());
            return;
        }
        
        // Database connection parameters
        String url = "jdbc:postgresql://localhost:5432/test_automation";
        String username = "postgres";
        String password = "admin";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ Connected to PostgreSQL database\n");
            
            // Show table structure
            showTableStructure(conn);
            
            // Query test suites
            queryTestSuites(conn);
            
            // Query scenarios
            queryScenarios(conn);
            
            // Query logs
            queryLogs(conn);
            
            // Query failure reasons
            queryFailureReasons(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            
            if (e.getMessage().contains("Connection refused")) {
                System.err.println("💡 Make sure PostgreSQL is running on localhost:5432");
            } else if (e.getMessage().contains("password")) {
                System.err.println("💡 Check PostgreSQL password - current: '" + password + "'");
            } else if (e.getMessage().contains("database") && e.getMessage().contains("does not exist")) {
                System.err.println("💡 Create database: CREATE DATABASE test_automation;");
            }
        }
    }
    
    private static void showTableStructure(Connection conn) {
        System.out.println("📊 DATABASE TABLES:");
        System.out.println("=" .repeat(60));
        
        String[] tables = {"test_suites", "test_scenarios", "test_logs", "test_failure_reasons"};
        
        for (String table : tables) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table)) {
                
                rs.next();
                System.out.println("📋 " + table + ": " + rs.getInt("count") + " records");
                
            } catch (SQLException e) {
                System.err.println("   Error checking " + table + ": " + e.getMessage());
            }
        }
        System.out.println();
    }
    
    private static void queryTestSuites(Connection conn) {
        System.out.println("🏁 TEST SUITES:");
        System.out.println("=" .repeat(60));
        
        String sql = """
            SELECT test_suite_id, engine, start_time, end_time, status, 
                   total_scenarios, passed_scenarios, failed_scenarios, skipped_scenarios
            FROM test_suites 
            ORDER BY start_time DESC 
            LIMIT 10
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No test suites found");
            } else {
                while (rs.next()) {
                    System.out.println("🔸 Suite: " + rs.getString("test_suite_id"));
                    System.out.println("   Engine: " + rs.getString("engine"));
                    System.out.println("   Status: " + rs.getString("status"));
                    System.out.println("   Start: " + rs.getTimestamp("start_time"));
                    if (rs.getTimestamp("end_time") != null) {
                        System.out.println("   End: " + rs.getTimestamp("end_time"));
                    }
                    System.out.println("   Results: " + rs.getInt("passed_scenarios") + " passed, " + 
                                     rs.getInt("failed_scenarios") + " failed, " + 
                                     rs.getInt("skipped_scenarios") + " skipped");
                    System.out.println();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying test suites: " + e.getMessage());
        }
    }
    
    private static void queryScenarios(Connection conn) {
        System.out.println("📋 SCENARIOS:");
        System.out.println("=" .repeat(60));
        
        String sql = """
            SELECT test_suite_id, scenario_name, feature_file, tags, 
                   start_time, end_time, status, error_message, engine
            FROM test_scenarios 
            ORDER BY start_time DESC 
            LIMIT 15
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No scenarios found");
            } else {
                while (rs.next()) {
                    System.out.println("🔸 " + rs.getString("scenario_name"));
                    System.out.println("   Suite: " + rs.getString("test_suite_id"));
                    System.out.println("   Engine: " + rs.getString("engine"));
                    System.out.println("   Status: " + rs.getString("status"));
                    System.out.println("   Feature: " + rs.getString("feature_file"));
                    if (rs.getString("error_message") != null && !rs.getString("error_message").isEmpty()) {
                        System.out.println("   Error: " + rs.getString("error_message"));
                    }
                    System.out.println();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying scenarios: " + e.getMessage());
        }
    }
    
    private static void queryLogs(Connection conn) {
        System.out.println("📝 LOGS:");
        System.out.println("=" .repeat(60));
        
        String sql = """
            SELECT test_suite_id, scenario_name, log_level, message, timestamp, engine
            FROM test_logs 
            ORDER BY timestamp DESC 
            LIMIT 20
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No logs found");
            } else {
                while (rs.next()) {
                    System.out.println("🔸 [" + rs.getTimestamp("timestamp") + "] " + 
                                     rs.getString("log_level").toUpperCase() + 
                                     " - " + rs.getString("message"));
                    System.out.println("   Suite: " + rs.getString("test_suite_id"));
                    System.out.println("   Engine: " + rs.getString("engine"));
                    System.out.println();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying logs: " + e.getMessage());
        }
    }
    
    private static void queryFailureReasons(Connection conn) {
        System.out.println("🔴 FAILURE REASONS:");
        System.out.println("=" .repeat(60));
        
        // Get top failure types
        String sql = """
            SELECT failure_type, COUNT(*) as count,
                   STRING_AGG(DISTINCT scenario_name, ', ') as affected_scenarios
            FROM test_failure_reasons 
            GROUP BY failure_type 
            ORDER BY count DESC 
            LIMIT 10
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No failure reasons found (all tests passed!)");
            } else {
                while (rs.next()) {
                    System.out.println("🔸 " + rs.getString("failure_type") + ": " + rs.getInt("count") + " occurrences");
                    System.out.println("   Scenarios: " + rs.getString("affected_scenarios"));
                    System.out.println();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying failure reasons: " + e.getMessage());
        }
        
        // Get recent failures
        String recentSql = """
            SELECT scenario_name, failure_type, error_message, timestamp, engine
            FROM test_failure_reasons 
            ORDER BY timestamp DESC 
            LIMIT 5
        """;
        
        System.out.println("📋 RECENT FAILURES:");
        System.out.println("-".repeat(40));
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(recentSql)) {
            
            if (!rs.isBeforeFirst()) {
                System.out.println("No recent failures found");
            } else {
                while (rs.next()) {
                    System.out.println("🔸 " + rs.getString("scenario_name"));
                    System.out.println("   Type: " + rs.getString("failure_type"));
                    System.out.println("   Engine: " + rs.getString("engine"));
                    System.out.println("   Time: " + rs.getTimestamp("timestamp"));
                    if (rs.getString("error_message") != null && !rs.getString("error_message").isEmpty()) {
                        System.out.println("   Error: " + rs.getString("error_message").substring(0, Math.min(100, rs.getString("error_message").length())) + "...");
                    }
                    System.out.println();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error querying recent failures: " + e.getMessage());
        }
    }
}
