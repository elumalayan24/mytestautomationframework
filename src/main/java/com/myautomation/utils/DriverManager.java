package com.myautomation.utils;

import com.myautomation.config.ConfigManager;
// Using fully qualified name to avoid conflict with this class name
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.time.Duration;
import java.util.Objects;

/**
 * Manages WebDriver instances and configurations.
 */
public class DriverManager {
    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get the WebDriver instance, creating it if necessary.
     */
    public static WebDriver getDriver() {
        if (Objects.isNull(driver.get())) {
            driver.set(createDriver());
        }
        return driver.get();
    }
    
    /**
     * Create a new WebDriver instance based on configuration.
     */
    private static WebDriver createDriver() {
        String browser = ConfigManager.getProperty("browser", "chrome").toLowerCase();
        boolean headless = ConfigManager.getBooleanProperty("headless", false);
        
        logger.info("Creating new WebDriver instance for {} (headless: {})", browser, headless);
        
        WebDriver driver;
        
        switch (browser) {
            case "firefox":
                System.setProperty("webdriver.gecko.driver", "drivers/geckodriver.exe");
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) firefoxOptions.addArguments("--headless");
                driver = new FirefoxDriver(firefoxOptions);
                break;
                
            case "safari":
                // Safari doesn't require a separate driver on macOS
                SafariOptions safariOptions = new SafariOptions();
                driver = new SafariDriver(safariOptions);
                break;
                
            case "edge":
                System.setProperty("webdriver.edge.driver", "drivers/msedgedriver.exe");
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) edgeOptions.addArguments("--headless");
                driver = new EdgeDriver(edgeOptions);
                break;
                
            case "chrome":
            default:
                System.setProperty("webdriver.chrome.driver", "drivers\\chromedriver.exe");
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) chromeOptions.addArguments("--headless");
                // Add common Chrome options
                chromeOptions.addArguments(
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--window-size=1920,1080"
                );
                driver = new ChromeDriver(chromeOptions);
        }
        
        // Common WebDriver settings
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        
        return driver;
    }

    /**
     * Take a screenshot and return the file path.
     */
    public static String takeScreenshot() {
        try {
            File screenshotFile = ((TakesScreenshot) getDriver())
                .getScreenshotAs(OutputType.FILE);
            
            String screenshotDir = ConfigManager.getProperty("screenshot.directory", "test-output/screenshots");
            File directory = new File(screenshotDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String screenshotPath = screenshotDir + "/screenshot_" + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(screenshotFile, new File(screenshotPath));
            return screenshotPath;
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
            return "Screenshot capture failed: " + e.getMessage();
        }
    }
    
    /**
     * Quit the WebDriver instance.
     */
    public static void quitDriver() {
        if (Objects.nonNull(driver.get())) {
            try {
                driver.get().quit();
                logger.info("WebDriver instance closed successfully");
            } catch (Exception e) {
                logger.error("Error while closing WebDriver: {}", e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
}
