package com.myautomation.session;

import com.myautomation.utils.ErrorLogger;

import com.myautomation.utils.ErrorLogger;

/**
 * Represents a session in the application.
 */
public interface Session {
    /**
     * Gets the unique identifier for this session.
     * @return the session ID
     */
    String getId();
    
    /**
     * Checks if the session is valid.
     * @return true if the session is valid, false otherwise
     */
    boolean isValid();
    
    /**
     * Invalidates the session.
     */
    void invalidate();
    
    /**
     * Gets the creation time of the session in milliseconds since epoch.
     * @return the creation time
     */
    long getCreationTime();
    
    /**
     * Gets the last accessed time of the session in milliseconds since epoch.
     * @return the last accessed time
     */
    long getLastAccessedTime();
    
    /**
     * Updates the last accessed time to the current time.
     */
    void updateLastAccessedTime();
    
    /**
     * Gets the maximum inactive interval in seconds.
     * @return the maximum inactive interval
     */
    int getMaxInactiveInterval();
    
    /**
     * Sets the maximum inactive interval in seconds.
     * @param interval the maximum inactive interval in seconds
     */
    void setMaxInactiveInterval(int interval);
    
    /**
     * Logs an informational message with session context.
     * @param message the message to log
     */
    default void logInfo(String message) {
        ErrorLogger.info("[Session: " + getId() + "] " + message);
    }
    
    /**
     * Logs an informational message with parameters and session context.
     * @param format the format string
     * @param args   the arguments
     */
    default void logInfo(String format, Object... args) {
        ErrorLogger.info("[Session: " + getId() + "] " + String.format(format, args));
    }
}
