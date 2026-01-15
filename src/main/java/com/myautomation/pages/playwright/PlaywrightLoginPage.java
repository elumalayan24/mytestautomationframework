package com.myautomation.pages.playwright;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class PlaywrightLoginPage extends BasePlaywrightPage {
    
    // Locators
    private static final String USERNAME_INPUT = "#user-name";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "#login-button";
    private static final String ERROR_MESSAGE = "[data-test='error']";
    
    public PlaywrightLoginPage(Page page) {
        super(page);
    }
    
    /**
     * Enter username
     * @param username The username to enter
     */
    public void enterUsername(String username) {
        page.locator(USERNAME_INPUT).fill(username);
    }
    
    /**
     * Enter password
     * @param password The password to enter
     */
    public void enterPassword(String password) {
        page.locator(PASSWORD_INPUT).fill(password);
    }
    
    /**
     * Click login button
     */
    public void clickLoginButton() {
        page.locator(LOGIN_BUTTON).click();
    }
    
    /**
     * Get error message text
     * @return Error message text
     */
    public String getErrorMessage() {
        return page.locator(ERROR_MESSAGE).textContent();
    }
    
    /**
     * Check if login was successful by verifying we're on inventory page
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        waitForPageLoad();
        return getCurrentUrl().contains("inventory.html");
    }
    
    /**
     * Check if error message is displayed
     * @return true if error message is visible, false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        return page.locator(ERROR_MESSAGE).isVisible();
    }
}
