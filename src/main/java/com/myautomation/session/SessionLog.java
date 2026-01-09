package com.myautomation.session;

import com.myautomation.utils.ErrorLogger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging session-related activities.
 * Provides methods to log session events with timestamps and session context.
 */
public final class SessionLog {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                         .withZone(ZoneId.systemDefault());

    private SessionLog() {
        // Private constructor to prevent instantiation
    }

    /**
     * Logs a session event with the specified message.
     *
     * @param sessionId The ID of the session
     * @param message   The message to log
     */
    public static void log(String sessionId, String message) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        String logMessage = String.format("[%s] [Session: %s] %s", 
            timestamp, sessionId, message);
        ErrorLogger.info(logMessage);
    }

    /**
     * Logs a session event with a formatted message and arguments.
     *
     * @param sessionId The ID of the session
     * @param format    The format string
     * @param args      The arguments referenced by the format specifiers in the format string
     */
    public static void log(String sessionId, String format, Object... args) {
        String message = String.format(format, args);
        log(sessionId, message);
    }

    /**
     * Logs the creation of a new session.
     *
     * @param sessionId The ID of the created session
     */
    public static void sessionCreated(String sessionId) {
        log(sessionId, "Session created");
    }

    /**
     * Logs the invalidation of a session.
     *
     * @param sessionId The ID of the invalidated session
     */
    public static void sessionInvalidated(String sessionId) {
        log(sessionId, "Session invalidated");
    }

    /**
     * Logs when a session is accessed.
     *
     * @param sessionId The ID of the accessed session
     */
    public static void sessionAccessed(String sessionId) {
        log(sessionId, "Session accessed");
    }

    /**
     * Logs a session timeout event.
     *
     * @param sessionId The ID of the timed-out session
     */
    public static void sessionTimeout(String sessionId) {
        log(sessionId, "Session timed out");
    }
}
