package com.myautomation.tests;

import com.microsoft.playwright.*;
import com.myautomation.sessions.PlaywrightCucumberSession;
import org.junit.jupiter.api.*;

/**
 * Base JUnit test class that provides automatic Playwright setup and teardown
 * Extend this class in your JUnit tests to get automatic browser configuration
 */
public abstract class BasePlaywrightTest {

    protected static PlaywrightCucumberSession session;

    @BeforeAll
    static void setupClass() {
        session = new PlaywrightCucumberSession();
        session.setUp();
    }

    @BeforeEach
    void setupTest() {
        // Create fresh context and page for each test
        if (session != null) {
            session.setUp();
        }
    }

    @AfterEach
    void tearDownTest() {
        // Clean up context and page after each test
        if (session != null) {
            session.tearDown();
        }
    }

    @AfterAll
    static void tearDownClass() {
        // Clean up all resources
        if (session != null) {
            PlaywrightCucumberSession.cleanupAll();
        }
    }

    /**
     * Get the current page instance
     */
    protected Page getPage() {
        return session != null ? session.getPage() : null;
    }

    /**
     * Get the browser context instance
     */
    protected BrowserContext getContext() {
        return session != null ? session.getContext() : null;
    }

    /**
     * Navigate to a URL
     */
    protected void navigateTo(String url) {
        if (session != null) {
            session.navigateTo(url);
        }
    }

    /**
     * Take a screenshot
     */
    protected void takeScreenshot(String name) {
        if (session != null) {
            session.takeScreenshot(name);
        }
    }

    /**
     * Wait for page to load
     */
    protected void waitForPageLoad() {
        if (session != null) {
            session.waitForPageLoad();
        }
    }
}
