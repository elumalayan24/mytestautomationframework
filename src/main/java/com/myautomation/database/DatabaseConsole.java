package com.myautomation.database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class DatabaseConsole {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("=== Test Automation PostgreSQL Console ===");
            System.out.println("1. View Recent Test Suites");
            System.out.println("2. View Test Suite Details");
            System.out.println("3. View Scenario History");
            System.out.println("4. View Test Logs");
            System.out.println("5. Search by Test Suite ID");
            System.out.println("6. Exit");
            
            while (true) {
                System.out.print("\nEnter your choice (1-6): ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (choice) {
                    case 1:
                        viewRecentTestSuites();
                        break;
                    case 2:
                        System.out.print("Enter Test Suite ID: ");
                        String suiteId = scanner.nextLine();
                        viewTestSuiteDetails(suiteId);
                        break;
                    case 3:
                        viewScenarioHistory();
                        break;
                    case 4:
                        System.out.print("Enter Test Suite ID (or press Enter for all): ");
                        String logSuiteId = scanner.nextLine();
                        viewTestLogs(logSuiteId.isEmpty() ? null : logSuiteId);
                        break;
                    case 5:
                        System.out.print("Enter Test Suite ID to search: ");
                        String searchId = scanner.nextLine();
                        searchByTestSuiteId(searchId);
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in database console: " + e.getMessage());
        } finally {
            DatabaseConfig.closeDataSource();
        }
    }
    
    private static void viewRecentTestSuites() {
        String sql = """
            SELECT test_suite_id, engine, status, total_scenarios, passed_scenarios, 
                   failed_scenarios, skipped_scenarios, start_time, end_time
            FROM test_suites 
            ORDER BY start_time DESC 
            LIMIT 10
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Recent Test Suites ===");
            System.out.printf("%-20s %-10s %-10s %-8s %-8s %-8s %-8s %-20s %-20s%n", 
                "Suite ID", "Engine", "Status", "Total", "Passed", "Failed", "Skipped", "Start Time", "End Time");
            System.out.println("-".repeat(140));
            
            while (rs.next()) {
                System.out.printf("%-20s %-10s %-10s %-8d %-8d %-8d %-8d %-20s %-20s%n",
                    rs.getString("test_suite_id"),
                    rs.getString("engine"),
                    rs.getString("status"),
                    rs.getInt("total_scenarios"),
                    rs.getInt("passed_scenarios"),
                    rs.getInt("failed_scenarios"),
                    rs.getInt("skipped_scenarios"),
                    formatTimestamp(rs.getTimestamp("start_time")),
                    formatTimestamp(rs.getTimestamp("end_time")));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching test suites: " + e.getMessage());
        }
    }
    
    private static void viewTestSuiteDetails(String suiteId) {
        String suiteSql = "SELECT * FROM test_suites WHERE test_suite_id = ?";
        String scenariosSql = "SELECT * FROM test_scenarios WHERE test_suite_id = ? ORDER BY start_time";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement suiteStmt = conn.prepareStatement(suiteSql);
             PreparedStatement scenarioStmt = conn.prepareStatement(scenariosSql)) {
            
            suiteStmt.setString(1, suiteId);
            ResultSet suiteRs = suiteStmt.executeQuery();
            
            if (suiteRs.next()) {
                System.out.println("\n=== Test Suite Details ===");
                System.out.println("Suite ID: " + suiteRs.getString("test_suite_id"));
                System.out.println("Engine: " + suiteRs.getString("engine"));
                System.out.println("Status: " + suiteRs.getString("status"));
                System.out.println("Start Time: " + formatTimestamp(suiteRs.getTimestamp("start_time")));
                System.out.println("End Time: " + formatTimestamp(suiteRs.getTimestamp("end_time")));
                System.out.println("Total Scenarios: " + suiteRs.getInt("total_scenarios"));
                System.out.println("Passed: " + suiteRs.getInt("passed_scenarios"));
                System.out.println("Failed: " + suiteRs.getInt("failed_scenarios"));
                System.out.println("Skipped: " + suiteRs.getInt("skipped_scenarios"));
                
                // Get scenarios
                scenarioStmt.setString(1, suiteId);
                ResultSet scenarioRs = scenarioStmt.executeQuery();
                
                System.out.println("\n=== Scenarios ===");
                System.out.printf("%-50s %-20s %-10s %-20s %-20s%n", 
                    "Scenario Name", "Feature File", "Status", "Start Time", "End Time");
                System.out.println("-".repeat(130));
                
                while (scenarioRs.next()) {
                    System.out.printf("%-50s %-20s %-10s %-20s %-20s%n",
                        truncate(scenarioRs.getString("scenario_name"), 50),
                        truncate(scenarioRs.getString("feature_file"), 20),
                        scenarioRs.getString("status"),
                        formatTimestamp(scenarioRs.getTimestamp("start_time")),
                        formatTimestamp(scenarioRs.getTimestamp("end_time")));
                }
                
            } else {
                System.out.println("Test Suite not found: " + suiteId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching test suite details: " + e.getMessage());
        }
    }
    
    private static void viewScenarioHistory() {
        String sql = """
            SELECT scenario_name, COUNT(*) as total_runs, 
                   SUM(CASE WHEN status = 'PASSED' THEN 1 ELSE 0 END) as passed,
                   SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed,
                   MAX(start_time) as last_run
            FROM test_scenarios 
            GROUP BY scenario_name 
            ORDER BY total_runs DESC 
            LIMIT 20
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Scenario History ===");
            System.out.printf("%-50s %-10s %-8s %-8s %-20s%n", 
                "Scenario Name", "Total Runs", "Passed", "Failed", "Last Run");
            System.out.println("-".repeat(110));
            
            while (rs.next()) {
                System.out.printf("%-50s %-10d %-8d %-8d %-20s%n",
                    truncate(rs.getString("scenario_name"), 50),
                    rs.getInt("total_runs"),
                    rs.getInt("passed"),
                    rs.getInt("failed"),
                    formatTimestamp(rs.getTimestamp("last_run")));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching scenario history: " + e.getMessage());
        }
    }
    
    private static void viewTestLogs(String suiteId) {
        String sql;
        if (suiteId != null) {
            sql = "SELECT * FROM test_logs WHERE test_suite_id = ? ORDER BY timestamp DESC LIMIT 50";
        } else {
            sql = "SELECT * FROM test_logs ORDER BY timestamp DESC LIMIT 50";
        }
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = suiteId != null ? 
                 conn.prepareStatement(sql) : conn.prepareStatement(sql)) {
            
            if (suiteId != null) {
                stmt.setString(1, suiteId);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("\n=== Test Logs ===");
            System.out.printf("%-20s %-10s %-50s %-30s%n", 
                "Timestamp", "Level", "Message", "Engine");
            System.out.println("-".repeat(120));
            
            while (rs.next()) {
                System.out.printf("%-20s %-10s %-50s %-30s%n",
                    formatTimestamp(rs.getTimestamp("timestamp")),
                    rs.getString("log_level"),
                    truncate(rs.getString("message"), 50),
                    rs.getString("engine"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching test logs: " + e.getMessage());
        }
    }
    
    private static void searchByTestSuiteId(String searchId) {
        String sql = """
            SELECT ts.*, COUNT(tc.id) as scenario_count
            FROM test_suites ts
            LEFT JOIN test_scenarios tc ON ts.test_suite_id = tc.test_suite_id
            WHERE ts.test_suite_id LIKE ?
            GROUP BY ts.test_suite_id
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + searchId + "%");
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("\n=== Search Results ===");
            System.out.printf("%-20s %-10s %-10s %-8s %-20s %-20s%n", 
                "Suite ID", "Engine", "Status", "Scenarios", "Start Time", "End Time");
            System.out.println("-".repeat(100));
            
            while (rs.next()) {
                System.out.printf("%-20s %-10s %-10s %-8d %-20s %-20s%n",
                    rs.getString("test_suite_id"),
                    rs.getString("engine"),
                    rs.getString("status"),
                    rs.getInt("scenario_count"),
                    formatTimestamp(rs.getTimestamp("start_time")),
                    formatTimestamp(rs.getTimestamp("end_time")));
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching test suites: " + e.getMessage());
        }
    }
    
    private static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }
    
    private static String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}
