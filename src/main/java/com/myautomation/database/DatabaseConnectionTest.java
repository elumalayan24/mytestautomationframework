package com.myautomation.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/test_automation";
        String username = "postgres";
        String password = "admin";
        
        System.out.println("Testing PostgreSQL connection...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connection successful!");
            System.out.println("Database: " + conn.getCatalog());
            System.out.println("URL: " + conn.getMetaData().getURL());
            conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            
            if (e.getMessage().contains("password")) {
                System.err.println("\n🔧 Password Issues:");
                System.err.println("1. Check if password is correct");
                System.err.println("2. Try password: 'postgres' (default)");
                System.err.println("3. Try password: '' (empty)");
                System.err.println("4. Try password: 'admin123'");
            }
            
            if (e.getMessage().contains("database") && e.getMessage().contains("does not exist")) {
                System.err.println("\n🔧 Database Issues:");
                System.err.println("1. Connect to PostgreSQL and run: CREATE DATABASE test_automation;");
                System.err.println("2. Or try connecting to default 'postgres' database first");
            }
        }
    }
}
