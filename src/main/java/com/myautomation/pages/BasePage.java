package com.myautomation.pages;

import com.microsoft.playwright.Page;
import com.myautomation.utils.PerformanceMonitor;
import com.myautomation.utils.TestDataCache;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BasePage {
    protected Page page;
    private static final ConcurrentHashMap<String, String> elementCache = new ConcurrentHashMap<>();
    
    public BasePage(Page page) {
        this.page = page;
    }
    
    protected void waitForElement(String selector, int timeoutMs) {
        PerformanceMonitor.startTimer("wait_element");
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
        } finally {
            PerformanceMonitor.endTimer("wait_element");
        }
    }
    
    protected void click(String selector) {
        PerformanceMonitor.startTimer("click_element");
        try {
            page.click(selector);
        } finally {
            PerformanceMonitor.endTimer("click_element");
        }
    }
    
    protected void type(String selector, String text) {
        PerformanceMonitor.startTimer("type_element");
        try {
            page.fill(selector, text);
        } finally {
            PerformanceMonitor.endTimer("type_element");
        }
    }
    
    protected String getText(String selector) {
        PerformanceMonitor.startTimer("get_text");
        try {
            return page.textContent(selector);
        } finally {
            PerformanceMonitor.endTimer("get_text");
        }
    }
    
    protected boolean isVisible(String selector) {
        PerformanceMonitor.startTimer("is_visible");
        try {
            return page.isVisible(selector);
        } finally {
            PerformanceMonitor.endTimer("is_visible");
        }
    }
    
    protected void navigate(String url) {
        PerformanceMonitor.startTimer("navigate");
        try {
            page.navigate(url);
        } finally {
            PerformanceMonitor.endTimer("navigate");
        }
    }
    
    protected String getCachedData(String key) {
        return TestDataCache.get(key, String.class);
    }
    
    protected void cacheData(String key, String value) {
        TestDataCache.put(key, value);
    }
}
