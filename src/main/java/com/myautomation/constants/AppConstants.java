package com.myautomation.constants;

import com.myautomation.config.ConfigManager;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Holds application-wide constants and default values.
 */
public final class AppConstants {

    private AppConstants() {} // prevent instantiation

    // Timeouts (default values, can be overridden in config.properties)
    public static final int DEFAULT_TIMEOUT = ConfigManager.getIntProperty("timeout.default", 10);
    public static final int PAGE_LOAD_TIMEOUT = ConfigManager.getIntProperty("timeout.pageLoad", 30);
    public static final int SCRIPT_TIMEOUT = ConfigManager.getIntProperty("timeout.script", 10);

    // Paths
    public static final Path CONFIG_FILE_PATH = Paths.get(
            ConfigManager.getProperty("config.file", "src/main/resources/config.properties"));
    public static final Path TEST_DATA_PATH = Paths.get(
            ConfigManager.getProperty("testdata.path", "src/test/resources/testdata/"));
    public static final Path SCREENSHOT_PATH = Paths.get(
            ConfigManager.getProperty("screenshots.path", "test-output/screenshots/"));

    // Default browser (can be overridden in config.properties)
    public static final String BROWSER = ConfigManager.getProperty("browser", "chrome");

    // Other constants
    public static final String BASE_URL = ConfigManager.getProperty("base.url", "https://example.com");
}
