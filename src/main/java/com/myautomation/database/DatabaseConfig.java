package com.myautomation.database;

import com.myautomation.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    
    private static HikariDataSource dataSource;
    private static final String DB_URL = ConfigManager.getProperty("database.url", "jdbc:postgresql://localhost:5432/test_automation");
    private static final String DB_USER = ConfigManager.getProperty("database.username", "postgres");
    private static final String DB_PASSWORD = ConfigManager.getProperty("database.password", "");
    
    static {
        try {
            initializeDataSource();
            createTables();
            
            // Add shutdown hook to properly close database connection
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("JVM shutdown - closing database connection");
                closeDataSource();
            }));
            
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            System.err.println("Tests will continue without database logging.");
            dataSource = null;
        }
    }
    
    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setDriverClassName("org.postgresql.Driver");
        
        // Connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1200000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection not available. Check configuration and ensure PostgreSQL is running.");
        }
        return dataSource.getConnection();
    }
    
    private static void createTables() {
        String createTestSuitesTable = """
            CREATE TABLE IF NOT EXISTS test_suites (
                id BIGSERIAL PRIMARY KEY,
                test_suite_id VARCHAR(100) NOT NULL UNIQUE,
                engine VARCHAR(50) NOT NULL,
                start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                end_time TIMESTAMP,
                status VARCHAR(20) DEFAULT 'RUNNING',
                total_scenarios INTEGER DEFAULT 0,
                passed_scenarios INTEGER DEFAULT 0,
                failed_scenarios INTEGER DEFAULT 0,
                skipped_scenarios INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createScenariosTable = """
            CREATE TABLE IF NOT EXISTS test_scenarios (
                id BIGSERIAL PRIMARY KEY,
                test_suite_id VARCHAR(100) NOT NULL,
                scenario_name VARCHAR(500) NOT NULL,
                feature_file VARCHAR(200),
                tags VARCHAR(500),
                start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                end_time TIMESTAMP,
                status VARCHAR(20) DEFAULT 'RUNNING',
                error_message TEXT,
                engine VARCHAR(50) NOT NULL,
                FOREIGN KEY (test_suite_id) REFERENCES test_suites(test_suite_id)
            )
        """;
        
        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS test_logs (
                id BIGSERIAL PRIMARY KEY,
                test_suite_id VARCHAR(100) NOT NULL,
                scenario_name VARCHAR(500),
                log_level VARCHAR(20) NOT NULL,
                message TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                engine VARCHAR(50),
                FOREIGN KEY (test_suite_id) REFERENCES test_suites(test_suite_id)
            )
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createTestSuitesTable);
            stmt.execute(createScenariosTable);
            stmt.execute(createLogsTable);
            
            System.out.println("PostgreSQL tables created/verified successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to create PostgreSQL tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("PostgreSQL connection pool closed");
        }
    }
    
    public static boolean isConnectionValid() {
        if (dataSource == null) return false;
        try (Connection conn = getConnection()) {
            return conn.isValid(2); // 2 seconds timeout
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static boolean isDatabaseAvailable() {
        return dataSource != null;
    }
}
