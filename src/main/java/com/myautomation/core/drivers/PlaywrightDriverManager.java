package com.myautomation.core.drivers;

import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType;

public class PlaywrightDriverManager {
    private static ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    
    /**
     * Initialize Playwright and create a new browser instance
     * @return Page instance
     */
    public static Page getDriver() {
        if (playwrightThreadLocal.get() == null) {
            Playwright playwright = Playwright.create();
            playwrightThreadLocal.set(playwright);
            
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(100)); // Slow down for visibility
            browserThreadLocal.set(browser);
            
            BrowserContext context = browser.newContext();
            contextThreadLocal.set(context);
            
            Page page = context.newPage();
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
     * Close the current browser
     */
    public static void closeBrowser() {
        Browser browser = browserThreadLocal.get();
        if (browser != null) {
            browser.close();
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
}
