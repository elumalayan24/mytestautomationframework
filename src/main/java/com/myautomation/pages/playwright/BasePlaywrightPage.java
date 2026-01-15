package com.myautomation.pages.playwright;

import com.microsoft.playwright.Page;

public abstract class BasePlaywrightPage {
    protected Page page;
    
    public BasePlaywrightPage(Page page) {
        this.page = page;
    }
    
    /**
     * Navigate to a URL
     * @param url The URL to navigate to
     */
    public void navigate(String url) {
        page.navigate(url);
    }
    
    /**
     * Get the current URL
     * @return Current page URL
     */
    public String getCurrentUrl() {
        return page.url();
    }
    
    /**
     * Get the page title
     * @return Page title
     */
    public String getTitle() {
        return page.title();
    }
    
    /**
     * Wait for page to load
     */
    public void waitForPageLoad() {
        page.waitForLoadState();
    }
}
