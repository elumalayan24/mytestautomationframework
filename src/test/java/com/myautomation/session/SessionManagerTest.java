package com.myautomation.session;

import org.testng.annotations.*;
import static org.testng.Assert.*;

/**
 * Test class for SessionManager implementation.
 */
public class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeClass
    public void setup() {
        // Initialize once before all tests
        sessionManager = new SessionManager();
    }

    @AfterClass
    public void cleanup() {
        // Cleanup after all tests
        if (sessionManager != null) {
            sessionManager.shutdown();
        }
    }

    @BeforeMethod
    public void init() {
        // Reset state before each test
        sessionManager.clearSessions();
    }

    @Test(description = "Should create a new session")
    public void shouldCreateNewSession() {
        String sessionId = "test-session-1";

        Session session = sessionManager.createSession(sessionId);

        assertNotNull(session, "Session should not be null");
        assertEquals(session.getId(), sessionId, "Session ID should match");
        assertTrue(sessionManager.hasSession(sessionId),
                "Session should exist in manager");
    }

    @Test(description = "Should retrieve existing session")
    public void shouldRetrieveExistingSession() {
        String sessionId = "test-session-2";
        sessionManager.createSession(sessionId);

        Session session = sessionManager.getSession(sessionId);

        assertNotNull(session, "Session should be retrieved");
        assertEquals(session.getId(), sessionId, "Session ID should match");
    }

    @Test(description = "Should remove session")
    public void shouldRemoveSession() {
        String sessionId = "test-session-3";
        sessionManager.createSession(sessionId);

        boolean removed = sessionManager.removeSession(sessionId);

        assertTrue(removed, "Session should be removed successfully");
        assertFalse(sessionManager.hasSession(sessionId),
                "Session should not exist after removal");
    }

    @Test(description = "Should handle non-existent session")
    public void shouldHandleNonExistentSession() {
        String nonExistentId = "non-existent-id";

        Session session = sessionManager.getSession(nonExistentId);

        assertNull(session, "Non-existent session should return null");
    }
}
