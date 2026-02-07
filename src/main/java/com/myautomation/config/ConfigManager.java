package com.myautomation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Modern Config Manager for test automation
 * Supports default properties, type-safe getters, and environment overrides.
 */
public final class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties props = new Properties();
    private static final String DEFAULT_CONFIG = "config.properties";

    static {
        loadProperties(null);
        // Also load database-specific properties
        loadDatabaseProperties();
    }

    private ConfigManager() {} // prevent instantiation

    /**
     * Load database-specific properties
     */
    private static void loadDatabaseProperties() {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                log.info("Database configuration loaded successfully from database.properties");
            } else {
                log.warn("database.properties not found, using defaults");
            }
        } catch (Exception e) {
            log.warn("Failed to load database properties, using defaults: " + e.getMessage());
        }
    }

    /**
     * Load properties from classpath or optional external file path
     */
    public static void loadProperties(String externalPath) {
        try (InputStream input = externalPath != null
                ? new java.io.FileInputStream(externalPath)
                : ConfigManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG)) {

            if (input == null) {
                throw new RuntimeException("Unable to find config file: " +
                        (externalPath != null ? externalPath : DEFAULT_CONFIG));
            }
            props.load(input);
            log.info("Configuration loaded successfully from {}",
                    externalPath != null ? externalPath : DEFAULT_CONFIG);
        } catch (Exception e) {
            log.error("Failed to load config properties", e);
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            String value = props.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for key '{}', using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Reload properties dynamically (optional for long-running tests)
     */
    public static void reload() {
        log.info("Reloading configuration...");
        loadProperties(null);
    }
}
