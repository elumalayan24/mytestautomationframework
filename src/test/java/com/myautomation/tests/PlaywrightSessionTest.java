package com.myautomation.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample test class demonstrating BasePlaywrightTest usage
 */
public class PlaywrightSessionTest extends BasePlaywrightTest {

    @Test
    @DisplayName("Basic Navigation Test")
    @Tag("smoke")
    void basicNavigationTest() {
        navigateTo("https://www.example.com");
        
        Page page = getPage();
        String title = page.title();
        assertTrue(title.contains("Example"), "Page title should contain 'Example'");
        
        System.out.println("✓ Basic navigation test passed");
    }

    @Test
    @DisplayName("Page Content Verification")
    @Tag("regression")
    void pageContentVerification() {
        navigateTo("https://www.example.com");
        
        Page page = getPage();
        // Check for specific elements
        Locator heading = page.locator("h1");
        assertTrue(heading.count() > 0, "Page should have an h1 element");
        
        String headingText = heading.textContent();
        assertNotNull(headingText, "Heading should have text");
        
        // Take screenshot for documentation
        takeScreenshot("page_content_verification");
        
        System.out.println("✓ Page content verification passed");
    }

    @Test
    @DisplayName("URL and Title Validation")
    @Tag("validation")
    void urlAndTitleValidation() {
        String expectedUrl = "https://www.example.com";
        navigateTo(expectedUrl);
        
        Page page = getPage();
        // Validate current URL
        String currentUrl = page.url();
        assertEquals(expectedUrl, currentUrl, "Current URL should match expected URL");
        
        // Validate title
        String title = page.title();
        assertNotNull(title, "Page title should not be null");
        assertFalse(title.isEmpty(), "Page title should not be empty");
        
        System.out.println("✓ URL and title validation passed");
    }

    @Test
    @DisplayName("Element Interaction Test")
    @Tag("interaction")
    void elementInteractionTest() {
        navigateTo("https://www.example.com");
        
        Page page = getPage();
        // Find and interact with links
        Locator links = page.locator("a");
        int linkCount = links.count();
        assertTrue(linkCount > 0, "Page should have links");
        
        // Get text of first link
        if (linkCount > 0) {
            String firstLinkText = links.first().textContent();
            System.out.println("First link text: " + firstLinkText);
        }
        
        // Take screenshot after interaction
        takeScreenshot("element_interaction");
        
        System.out.println("✓ Element interaction test passed");
    }

    @Test
    @DisplayName("Browser Session Info")
    @Tag("info")
    void browserSessionInfo() {
        navigateTo("https://www.example.com");
        
        Page page = getPage();
        // Display session information
        System.out.println("=== Browser Session Information ===");
        System.out.println("User Agent: " + page.evaluate("navigator.userAgent"));
        System.out.println("Viewport: " + page.viewportSize());
        System.out.println("Current URL: " + page.url());
        System.out.println("Page Title: " + page.title());
        
        // Screenshot for session documentation
        takeScreenshot("browser_session_info");
        
        System.out.println("✓ Browser session info test passed");
    }
}
