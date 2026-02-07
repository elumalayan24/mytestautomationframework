package com.myautomation.core.drivers;

import com.myautomation.config.ConfigManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;

public class MobileDriverFactory {
    
    private static final ThreadLocal<AppiumDriver> mobileDriver = new ThreadLocal<>();
    
    private MobileDriverFactory() {} // Prevent instantiation
    
    public static AppiumDriver getMobileDriver() {
        if (mobileDriver.get() == null) {
            String platform = ConfigManager.getProperty("mobile.platform", "android").toLowerCase();
            String appiumServerUrl = ConfigManager.getProperty("appium.server.url", "http://localhost:4723/wd/hub");
            
            try {
                switch (platform) {
                    case "android":
                        mobileDriver.set(createAndroidDriver(appiumServerUrl));
                        break;
                    case "ios":
                        mobileDriver.set(createIOSDriver(appiumServerUrl));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported mobile platform: " + platform);
                }
                
                // Set timeouts
                mobileDriver.get().manage().timeouts()
                    .implicitlyWait(Duration.ofSeconds(ConfigManager.getIntProperty("mobile.timeout.implicit", 10)))
                    .pageLoadTimeout(Duration.ofSeconds(ConfigManager.getIntProperty("mobile.timeout.pageLoad", 30)));
                    
            } catch (Exception e) {
                throw new RuntimeException("Failed to create mobile driver: " + e.getMessage(), e);
            }
        }
        return mobileDriver.get();
    }
    
    private static AndroidDriver createAndroidDriver(String appiumServerUrl) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // Android capabilities
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("deviceName", ConfigManager.getProperty("android.device.name", "Android Device"));
        capabilities.setCapability("appPackage", ConfigManager.getProperty("android.app.package", ""));
        capabilities.setCapability("appActivity", ConfigManager.getProperty("android.app.activity", ""));
        
        // App file path or app package
        String appPath = ConfigManager.getProperty("android.app.path", "");
        if (!appPath.isEmpty()) {
            capabilities.setCapability("app", appPath);
        }
        
        // Additional capabilities
        capabilities.setCapability("noReset", ConfigManager.getBooleanProperty("mobile.noReset", false));
        capabilities.setCapability("fullReset", ConfigManager.getBooleanProperty("mobile.fullReset", false));
        capabilities.setCapability("newCommandTimeout", 60);
        
        return new AndroidDriver(new URL(appiumServerUrl), capabilities);
    }
    
    private static IOSDriver createIOSDriver(String appiumServerUrl) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // iOS capabilities
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("deviceName", ConfigManager.getProperty("ios.device.name", "iPhone"));
        capabilities.setCapability("bundleId", ConfigManager.getProperty("ios.bundle.id", ""));
        
        // App file path or bundle ID
        String appPath = ConfigManager.getProperty("ios.app.path", "");
        if (!appPath.isEmpty()) {
            capabilities.setCapability("app", appPath);
        }
        
        // Additional capabilities
        capabilities.setCapability("noReset", ConfigManager.getBooleanProperty("mobile.noReset", false));
        capabilities.setCapability("fullReset", ConfigManager.getBooleanProperty("mobile.fullReset", false));
        capabilities.setCapability("newCommandTimeout", 60);
        
        return new IOSDriver(new URL(appiumServerUrl), capabilities);
    }
    
    public static void quitMobileDriver() {
        AppiumDriver driver = mobileDriver.get();
        if (driver != null) {
            driver.quit();
            mobileDriver.remove();
        }
    }
    
    public static boolean isMobileDriverInitialized() {
        return mobileDriver.get() != null;
    }
}
