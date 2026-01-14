package com.myautomation.utils;

/**
 * Utility class for error and info logging.
 */
public class ErrorLogger {
    private ErrorLogger() {
        // Private constructor to prevent instantiation
    }

    /**
     * Logs an informational message.
     * @param message The message to log
     */
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * Logs an error message.
     * @param message The error message to log
     * @param e The exception (can be null)
     */
    public static void error(String message, Throwable e) {
        System.err.println("[ERROR] " + message);
        if (e != null) {
            e.printStackTrace();
        }
    }
}
