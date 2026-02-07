package com.myautomation.core.drivers;

import com.myautomation.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.microsoft.playwright.*;
import io.appium.java_client.AppiumDriver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class DriverFactory {

    // ----------------- Selenium -----------------
    private static final ThreadLocal<WebDriver> seleniumDriver = new ThreadLocal<>();

    // ----------------- Playwright -----------------
    private static final ThreadLocal<Page> playwrightPage = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> playwrightContext = new ThreadLocal<>();
    private static final ThreadLocal<Browser> playwrightBrowser = new ThreadLocal<>();
    private static final ThreadLocal<Playwright> playwrightInstance = new ThreadLocal<>();

    private static final BlockingQueue<Browser> playwrightPool = new LinkedBlockingQueue<>(5);
    private static final AtomicInteger activePlaywrightBrowsers = new AtomicInteger(0);
    private static final int MAX_PLAYWRIGHT_BROWSERS = 5;

    private DriverFactory() {} // Prevent instantiation

    // ----------------- Public API -----------------
    public static Object getDriver() {
        String engine = ConfigManager.getProperty("engine", "selenium").toLowerCase();
        return switch (engine) {
            case "playwright" -> getPlaywrightPage();
            case "mobile" -> MobileDriverFactory.getMobileDriver();
            default -> getSeleniumDriver();
        };
    }

    // ----------------- Selenium Methods -----------------
    private static WebDriver getSeleniumDriver() {
        if (seleniumDriver.get() == null) {
            String browser = ConfigManager.getProperty("browser", "chrome").toLowerCase();
            switch (browser) {
                case "chrome" -> {
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions options = new ChromeOptions();
                    if (ConfigManager.getBooleanProperty("headless", false)) {
                        options.addArguments("--headless=new");
                    }
                    options.addArguments("--disable-notifications", "--start-maximized");
                    seleniumDriver.set(new ChromeDriver(options));
                }
                case "firefox" -> {
                    WebDriverManager.firefoxdriver().setup();
                    seleniumDriver.set(new FirefoxDriver());
                }
                default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
            }

            // Timeouts
            seleniumDriver.get().manage().timeouts()
                    .implicitlyWait(java.time.Duration.ofSeconds(
                            ConfigManager.getIntProperty("timeout.default", 10)))
                    .pageLoadTimeout(java.time.Duration.ofSeconds(
                            ConfigManager.getIntProperty("timeout.pageLoad", 30)))
                    .scriptTimeout(java.time.Duration.ofSeconds(
                            ConfigManager.getIntProperty("timeout.script", 10)));
        }
        return seleniumDriver.get();
    }

    public static void quitSeleniumDriver() {
        WebDriver driver = seleniumDriver.get();
        if (driver != null) {
            driver.quit();
            seleniumDriver.remove();
        }
    }

    // ----------------- Playwright Methods -----------------
    private static Page getPlaywrightPage() {
        if (playwrightInstance.get() == null) {
            Playwright playwright = Playwright.create();
            playwrightInstance.set(playwright);

            Browser browser = playwrightPool.poll();
            if (browser == null && activePlaywrightBrowsers.get() < MAX_PLAYWRIGHT_BROWSERS) {
                BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                        .setHeadless(ConfigManager.getBooleanProperty("headless", true));
                browser = playwright.chromium().launch(options);
                activePlaywrightBrowsers.incrementAndGet();
            }
            playwrightBrowser.set(browser);
        }

        if (playwrightContext.get() == null) {
            BrowserContext context = playwrightBrowser.get().newContext();
            playwrightContext.set(context);
        }

        if (playwrightPage.get() == null) {
            Page page = playwrightContext.get().newPage();
            playwrightPage.set(page);
        }

        return playwrightPage.get();
    }

    public static void quitPlaywright() {
        try {
            if (playwrightPage.get() != null) {
                playwrightPage.get().close();
                playwrightPage.remove();
            }
            if (playwrightContext.get() != null) {
                playwrightContext.get().close();
                playwrightContext.remove();
            }
            if (playwrightBrowser.get() != null) {
                if (playwrightPool.size() < MAX_PLAYWRIGHT_BROWSERS) {
                    playwrightPool.offer(playwrightBrowser.get());
                } else {
                    playwrightBrowser.get().close();
                    activePlaywrightBrowsers.decrementAndGet();
                }
                playwrightBrowser.remove();
            }
            if (playwrightInstance.get() != null) {
                playwrightInstance.get().close();
                playwrightInstance.remove();
            }
        } catch (Exception e) {
            System.err.println("Error quitting Playwright: " + e.getMessage());
        }
    }

    // ----------------- Cleanup -----------------
    public static void unload() {
        quitSeleniumDriver();
        quitPlaywright();
        MobileDriverFactory.quitMobileDriver();
    }

    public static void shutdownPlaywrightPool() {
        Browser browser;
        while ((browser = playwrightPool.poll()) != null) {
            browser.close();
            activePlaywrightBrowsers.decrementAndGet();
        }
    }
}

