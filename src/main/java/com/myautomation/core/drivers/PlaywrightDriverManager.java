package com.myautomation.core.drivers;

import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaywrightDriverManager {
    private static ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    
    // Performance optimization: Browser pool for reuse
    private static final BlockingQueue<Browser> browserPool = new LinkedBlockingQueue<>(5);
    private static final AtomicInteger activeBrowsers = new AtomicInteger(0);
    private static final int MAX_BROWSERS = 5;
    
    // Performance monitoring
    private static final ConcurrentHashMap<String, Long> performanceMetrics = new ConcurrentHashMap<>();
    
    /**
     * Initialize Playwright and create a new browser instance
     * @return Page instance
     */
    public static Page getDriver() {
        if (playwrightThreadLocal.get() == null) {
            Playwright playwright = Playwright.create();
            playwrightThreadLocal.set(playwright);
            
            Browser browser = browserPool.poll(); // Try to reuse browser from pool
            if (browser == null && activeBrowsers.get() < MAX_BROWSERS) {
                long startTime = System.currentTimeMillis();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true) // Headless for better performance
                        .setSlowMo(0) // Remove artificial delay
                        .setArgs(java.util.Arrays.asList(
                            "--disable-blink-features=AutomationControlled",
                            "--disable-extensions",
                            "--disable-gpu",
                            "--no-sandbox",
                            "--disable-dev-shm-usage"
                        )));
                activeBrowsers.incrementAndGet();
                performanceMetrics.put("browserLaunchTime", System.currentTimeMillis() - startTime);
            }
            browserThreadLocal.set(browser);
        }
        
        // Create new context and page for each scenario if they don't exist
        if (contextThreadLocal.get() == null) {
            BrowserContext context = browserThreadLocal.get().newContext();
            contextThreadLocal.set(context);
        }
        
        if (pageThreadLocal.get() == null) {
            Page page = contextThreadLocal.get().newPage();
            pageThreadLocal.set(page);
        }
        
        return pageThreadLocal.get();
    }
    
    /**
     * Get the current Playwright instance
     * @return Playwright instance
     */
    public static Playwright getPlaywright() {
        return playwrightThreadLocal.get();
    }
    
    /**
     * Get the current browser instance
     * @return Browser instance
     */
    public static Browser getBrowser() {
        return browserThreadLocal.get();
    }
    
    /**
     * Get the current browser context
     * @return BrowserContext instance
     */
    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }
    
    /**
     * Close the current page
     */
    public static void closePage() {
        Page page = pageThreadLocal.get();
        if (page != null) {
            page.close();
            pageThreadLocal.remove();
        }
    }
    
    /**
     * Close the current browser context
     */
    public static void closeContext() {
        BrowserContext context = contextThreadLocal.get();
        if (context != null) {
            context.close();
            contextThreadLocal.remove();
        }
    }
    
    /**
     * Close the current browser and return to pool if available
     */
    public static void closeBrowser() {
        Browser browser = browserThreadLocal.get();
        if (browser != null) {
            try {
                // Return browser to pool for reuse instead of closing
                if (browserPool.size() < MAX_BROWSERS && browser.isConnected()) {
                    browserPool.offer(browser);
                } else {
                    browser.close();
                    activeBrowsers.decrementAndGet();
                }
            } catch (Exception e) {
                System.err.println("Error returning browser to pool: " + e.getMessage());
                browser.close();
                activeBrowsers.decrementAndGet();
            }
            browserThreadLocal.remove();
        }
    }
    
    /**
     * Close Playwright and clean up all resources
     */
    public static void unload() {
        try {
            closePage();
            closeContext();
            closeBrowser();
            
            Playwright playwright = playwrightThreadLocal.get();
            if (playwright != null) {
                playwright.close();
                playwrightThreadLocal.remove();
            }
        } catch (Exception e) {
            System.err.println("Error during Playwright cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Get performance metrics
     */
    public static ConcurrentHashMap<String, Long> getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    /**
     * Close all browser instances in pool (call at test suite end)
     */
    public static void shutdownBrowserPool() {
        Browser browser;
        while ((browser = browserPool.poll()) != null) {
            try {
                browser.close();
                activeBrowsers.decrementAndGet();
            } catch (Exception e) {
                System.err.println("Error closing pooled browser: " + e.getMessage());
            }
        }
    }
}
