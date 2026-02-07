package com.myautomation.database;

import com.myautomation.config.ConfigManager;

public class DatabaseConfigTest {
    
    public static void main(String[] args) {
        System.out.println("=== Database Configuration Test ===");
        
        // Test ConfigManager loading
        System.out.println("Database URL: " + ConfigManager.getProperty("database.url"));
        System.out.println("Database Username: " + ConfigManager.getProperty("database.username"));
        System.out.println("Database Password: '" + ConfigManager.getProperty("database.password") + "'");
        
        // Test database connection
        try {
            boolean isAvailable = DatabaseConfig.isDatabaseAvailable();
            System.out.println("Database Available: " + isAvailable);
            
            if (isAvailable) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Database connection failed - check PostgreSQL setup");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
