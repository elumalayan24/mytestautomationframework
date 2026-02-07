package com.myautomation.session;

import com.myautomation.core.drivers.DriverFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of sessions in the application.
 */
public class SessionManager {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private volatile boolean isShutdown = false;
    private static final SessionManager INSTANCE = new SessionManager();
    
    /**
     * Gets the singleton instance of SessionManager.
     * @return the SessionManager instance
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Closes all sessions (static method for convenience in hooks).
     */
    public static void closeAll() {
        System.out.println("[SessionManager] Closing all sessions and cleaning up drivers...");
        getInstance().clearSessions();
        DriverFactory.unload();
    }
    
    /**
     * Creates a new session with the specified ID.
     * @param sessionId the ID for the new session
     * @return the newly created session
     * @throws IllegalStateException if the manager has been shut down
     */
    public synchronized Session createSession(String sessionId) {
        checkShutdown();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        if (sessions.containsKey(sessionId)) {
            throw new IllegalStateException("Session with ID " + sessionId + " already exists");
        }
        
        Session session = new DefaultSession(sessionId);
        sessions.put(sessionId, session);
        return session;
    }
    
    /**
     * Retrieves a session by its ID.
     * @param sessionId the ID of the session to retrieve
     * @return the session, or null if no session exists with the given ID
     */
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        
        Session session = sessions.get(sessionId);
        if (session != null && !session.isValid()) {
            sessions.remove(sessionId);
            return null;
        }
        
        if (session != null) {
            session.updateLastAccessedTime();
        }
        
        return session;
    }
    
    /**
     * Removes the session with the specified ID.
     * @param sessionId the ID of the session to remove
     * @return true if the session was removed, false if no session existed with the given ID
     */
    public boolean removeSession(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        
        Session session = sessions.remove(sessionId);
        if (session != null) {
            session.invalidate();
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a session with the given ID exists and is valid.
     * @param sessionId the ID of the session to check
     * @return true if a valid session exists with the given ID, false otherwise
     */
    public boolean hasSession(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        
        Session session = sessions.get(sessionId);
        return session != null && session.isValid();
    }
    
    /**
     * Shuts down the session manager, invalidating all active sessions.
     */
    public void shutdown() {
        if (isShutdown) {
            return;
        }
        
        isShutdown = true;
        sessions.values().forEach(Session::invalidate);
        sessions.clear();
    }
    
    /**
     * Clears all sessions from the manager.
     */
    public void clearSessions() {
        sessions.values().forEach(Session::invalidate);
        sessions.clear();
    }
    
    private void checkShutdown() {
        if (isShutdown) {
            throw new IllegalStateException("SessionManager has been shut down");
        }
    }
    
    /**
     * Default implementation of the Session interface.
     */
    private static class DefaultSession implements Session {
        private final String id;
        private final long creationTime;
        private volatile long lastAccessedTime;
        private volatile int maxInactiveInterval = 1800; // 30 minutes default
        private volatile boolean valid = true;
        
        public DefaultSession(String id) {
            this.id = id;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessedTime = this.creationTime;
        }
        
        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public boolean isValid() {
            if (!valid) {
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            long timeElapsed = (currentTime - lastAccessedTime) / 1000; // Convert to seconds
            
            return timeElapsed < maxInactiveInterval;
        }
        
        @Override
        public void invalidate() {
            this.valid = false;
        }
        
        @Override
        public long getCreationTime() {
            return creationTime;
        }
        
        @Override
        public long getLastAccessedTime() {
            return lastAccessedTime;
        }
        
        @Override
        public void updateLastAccessedTime() {
            this.lastAccessedTime = System.currentTimeMillis();
        }
        
        @Override
        public int getMaxInactiveInterval() {
            return maxInactiveInterval;
        }
        
        @Override
        public void setMaxInactiveInterval(int interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("Interval cannot be negative");
            }
            this.maxInactiveInterval = interval;
        }
    }
}
