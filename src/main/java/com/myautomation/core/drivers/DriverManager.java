package com.myautomation.core.drivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;

public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final AtomicBoolean cleanupRegistered = new AtomicBoolean(false);
    
    static {
        registerCleanupHooks();
    }
    
    private static void registerCleanupHooks() {
        if (cleanupRegistered.compareAndSet(false, true)) {
            // JVM Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[Shutdown Hook] Cleaning up WebDriver instances...");
                forceKillAllBrowsers();
            }, "DriverManager-Cleanup"));
            
            // Add finalizer for emergency cleanup
            System.gc(); // Suggest garbage collection to trigger finalizers
        }
    }

    private DriverManager() {
        // Private constructor to prevent instantiation
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            String browser = System.getProperty("browser", "chrome");
            try {
                switch (browser.toLowerCase()) {
                    case "chrome":
                        WebDriverManager.chromedriver().setup();
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--start-maximized");
                        options.addArguments("--disable-notifications");
                        // Add automation-specific flags for identification
                        options.addArguments("--remote-debugging-port=9222");
                        String userProfileDir = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "chrome-automation-profile-" + System.currentTimeMillis();
                        options.addArguments("--user-data-dir=" + userProfileDir);
                        driver.set(new ChromeDriver(options));
                        break;
                    case "firefox":
                        WebDriverManager.firefoxdriver().setup();
                        driver.set(new FirefoxDriver());
                        break;
                    case "remote":
                        // Add remote WebDriver configuration if needed
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported browser: " + browser);
                }
                
                // Common driver configurations
                driver.get().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                driver.get().manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
                driver.get().manage().window().maximize();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize WebDriver", e);
            }
        }
        return driver.get();
    }

    public static void unload() {
        if (driver.get() != null) {
            try {
                System.out.println("[DriverManager] Quitting WebDriver for thread: " + Thread.currentThread().getName());
                driver.get().quit();
            } catch (Exception e) {
                System.err.println("[DriverManager] Error quitting WebDriver: " + e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
    
    /**
     * Force cleanup all driver instances (for emergency cleanup)
     */
    public static void forceCleanupAll() {
        System.out.println("[DriverManager] Force cleanup initiated...");
        unload();
    }
    
    /**
     * Aggressive browser cleanup - kills only automation browser processes
     */
    public static void forceKillAllBrowsers() {
        System.out.println("[DriverManager] Force killing automation browser processes...");
        
        // First try graceful cleanup
        unload();
        
        // Then force kill only automation browser processes
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows - use PowerShell to find and kill Chrome processes with automation flags
                pb = new ProcessBuilder("powershell", "-Command", "Get-Process chrome | Where-Object {$_.CommandLine -like '*remote-debugging-port*'} | Stop-Process -Force");
                Process process = pb.start();
                process.waitFor();
                
                // Kill chromedriver processes (these are definitely automation-related)
                pb = new ProcessBuilder("taskkill", "/F", "/IM", "chromedriver.exe", "/T");
                process = pb.start();
                process.waitFor();
                
                // Kill Firefox processes with automation-related arguments
                pb = new ProcessBuilder("powershell", "-Command", "Get-Process firefox | Where-Object {$_.CommandLine -like '*-marionette*'} | Stop-Process -Force");
                process = pb.start();
                process.waitFor();
                
                // Kill geckodriver processes
                pb = new ProcessBuilder("taskkill", "/F", "/IM", "geckodriver.exe", "/T");
                process = pb.start();
                process.waitFor();
                
            } else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Unix-like systems - kill only Chrome processes with automation flags
                pb = new ProcessBuilder("pkill", "-f", "chrome.*remote-debugging-port");
                Process process = pb.start();
                process.waitFor();
                
                // Kill chromedriver processes
                pb = new ProcessBuilder("pkill", "-f", "chromedriver");
                process = pb.start();
                process.waitFor();
                
                // Kill Firefox processes with automation flags
                pb = new ProcessBuilder("pkill", "-f", "firefox.*-marionette");
                process = pb.start();
                process.waitFor();
                
                // Kill geckodriver processes
                pb = new ProcessBuilder("pkill", "-f", "geckodriver");
                process = pb.start();
                process.waitFor();
            }
            
            System.out.println("[DriverManager] Automation browser processes killed successfully");
            
        } catch (IOException | InterruptedException e) {
            System.err.println("[DriverManager] Error killing automation browser processes: " + e.getMessage());
        }
        
        // Final cleanup
        driver.remove();
    }
    
    // Add finalizer as last resort
    @Override
    protected void finalize() throws Throwable {
        try {
            forceKillAllBrowsers();
        } finally {
            super.finalize();
        }
    }
}
