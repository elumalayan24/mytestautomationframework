package com.myautomation.hooks;

import com.myautomation.utils.ScreenRecorderUtil;
import com.myautomation.sessions.PlaywrightCucumberSession;
import com.myautomation.sessions.PlaywrightSessionHolder;

import io.cucumber.java.*;

public class PlaywrightHooks {

    @Before("@playwright")
    public void beforeScenario(Scenario scenario) {
        ScreenRecorderUtil.start(scenario.getName());
        
        // Create and set up session
        PlaywrightCucumberSession session = new PlaywrightCucumberSession();
        session.setUp();
        PlaywrightSessionHolder.setSession(session);
    }

    @After("@playwright")
    public void afterScenario(Scenario scenario) {
        try {
            // Get session from holder
            PlaywrightCucumberSession session = PlaywrightSessionHolder.getSession();
            if (session != null) {
                // Take screenshot for all Playwright scenarios
                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String screenshotName = "playwright_" + scenario.getName() + "_" + timestamp;
                
                // Take screenshot using the session
                session.takeScreenshot(screenshotName);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to take Playwright screenshot: " + e.getMessage());
        } finally {
            // Always cleanup
            PlaywrightCucumberSession session = PlaywrightSessionHolder.getSession();
            if (session != null) {
                session.tearDown();
            }
            PlaywrightSessionHolder.clearSession();
            ScreenRecorderUtil.stop(scenario.getName());
        }
    }
}
