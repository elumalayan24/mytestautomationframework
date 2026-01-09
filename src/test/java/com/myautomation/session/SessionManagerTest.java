package com.myautomation.session;

import com.myautomation.session.Session;
import com.myautomation.session.SessionManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SessionManager implementation.
 */
public class SessionManagerTest {
    
    private static SessionManager sessionManager;
    
    @BeforeAll
    public static void setup() {
        // Initialize test resources before all tests
        sessionManager = new SessionManager();
    }
    
    @AfterAll
    public static void cleanup() {
        // Clean up resources after all tests
        if (sessionManager != null) {
            sessionManager.shutdown();
        }
    }
    
    @BeforeEach
    public void init() {
        // Reset session state before each test
        sessionManager.clearSessions();
    }
    
    @Test
    @DisplayName("Should create a new session")
    public void shouldCreateNewSession() {
        // Arrange
        String sessionId = "test-session-1";
        
        // Act
        Session session = sessionManager.createSession(sessionId);
        
        // Assert
        assertNotNull(session, "Session should not be null");
        assertEquals(sessionId, session.getId(), "Session ID should match");
        assertTrue(sessionManager.hasSession(sessionId), "Session should exist in manager");
    }
    
    @Test
    @DisplayName("Should retrieve existing session")
    public void shouldRetrieveExistingSession() {
        // Arrange
        String sessionId = "test-session-2";
        sessionManager.createSession(sessionId);
        
        // Act
        Session session = sessionManager.getSession(sessionId);
        
        // Assert
        assertNotNull(session, "Session should be retrieved");
        assertEquals(sessionId, session.getId(), "Session ID should match");
    }
    
    @Test
    @DisplayName("Should remove session")
    public void shouldRemoveSession() {
        // Arrange
        String sessionId = "test-session-3";
        sessionManager.createSession(sessionId);
        
        // Act
        boolean removed = sessionManager.removeSession(sessionId);
        
        // Assert
        assertTrue(removed, "Session should be removed successfully");
        assertFalse(sessionManager.hasSession(sessionId), "Session should not exist after removal");
    }
    
    @Test
    @DisplayName("Should handle non-existent session")
    public void shouldHandleNonExistentSession() {
        // Arrange
        String nonExistentId = "non-existent-id";
        
        // Act
        Session session = sessionManager.getSession(nonExistentId);
        
        // Assert
        assertNull(session, "Non-existent session should return null");
    }
}
