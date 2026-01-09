package com.myautomation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging errors and messages throughout the automation framework.
 * Provides different log levels and supports exception logging.
 */
public final class ErrorLogger {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogger.class);

    private ErrorLogger() {
        // Private constructor to prevent instantiation
    }

    /**
     * Logs a debug level message.
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Logs a debug level message with parameters.
     *
     * @param format the format string
     * @param args   the arguments
     */
    public static void debug(String format, Object... args) {
        logger.debug(format, args);
    }

    /**
     * Logs an info level message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Logs an info level message with parameters.
     *
     * @param format the format string
     * @param args   the arguments
     */
    public static void info(String format, Object... args) {
        logger.info(format, args);
    }

    /**
     * Logs a warning level message.
     *
     * @param message the warning message
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs a warning level message with parameters.
     *
     * @param format the format string
     * @param args   the arguments
     */
    public static void warn(String format, Object... args) {
        logger.warn(format, args);
    }

    /**
     * Logs an error level message.
     *
     * @param message the error message
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Logs an error level message with parameters.
     *
     * @param format the format string
     * @param args   the arguments
     */
    public static void error(String format, Object... args) {
        logger.error(format, args);
    }

    /**
     * Logs an exception with a custom message.
     *
     * @param message   the error message
     * @param throwable the exception to log
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Logs an exception.
     *
     * @param throwable the exception to log
     */
    public static void error(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
    }
}
