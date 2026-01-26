package com.myautomation.core.utils;

import com.microsoft.playwright.*;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Utility class for handling automation exceptions with retry logic
 * for both Selenium and Playwright operations.
 */
public final class AutomationExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AutomationExceptionHandler.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private AutomationExceptionHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Execute a Selenium action with retry logic and exception handling
     * @param action The action to execute
     * @param actionDescription Description of the action for logging
     * @param <T> Return type of the action
     * @return Result of the action
     * @throws RuntimeException if all retries fail
     */
    public static <T> T executeSeleniumAction(Supplier<T> action, String actionDescription) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                logger.debug("Executing Selenium action: {} (attempt {})", actionDescription, attempts + 1);
                T result = action.get();
                logger.debug("Successfully executed Selenium action: {}", actionDescription);
                return result;
            } catch (Exception e) {
                lastException = e;
                attempts++;
                logger.warn("Selenium action failed (attempt {}/{}): {} - {}", 
                    attempts, MAX_RETRIES, actionDescription, e.getMessage());
                
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }
                }
            }
        }

        logger.error("Selenium action failed after {} attempts: {}", MAX_RETRIES, actionDescription);
        throw new RuntimeException("Failed to execute Selenium action: " + actionDescription, lastException);
    }

    /**
     * Execute a Playwright action with retry logic and exception handling
     * @param action The action to execute
     * @param actionDescription Description of the action for logging
     * @param <T> Return type of the action
     * @return Result of the action
     * @throws RuntimeException if all retries fail
     */
    public static <T> T executePlaywrightAction(Supplier<T> action, String actionDescription) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                logger.debug("Executing Playwright action: {} (attempt {})", actionDescription, attempts + 1);
                T result = action.get();
                logger.debug("Successfully executed Playwright action: {}", actionDescription);
                return result;
            } catch (Exception e) {
                lastException = e;
                attempts++;
                logger.warn("Playwright action failed (attempt {}/{}): {} - {}", 
                    attempts, MAX_RETRIES, actionDescription, e.getMessage());
                
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }
                }
            }
        }

        logger.error("Playwright action failed after {} attempts: {}", MAX_RETRIES, actionDescription);
        throw new RuntimeException("Failed to execute Playwright action: " + actionDescription, lastException);
    }

    /**
     * Execute a Selenium action without return value with retry logic
     * @param action The action to execute
     * @param actionDescription Description of the action for logging
     */
    public static void executeSeleniumAction(Runnable action, String actionDescription) {
        executeSeleniumAction(() -> {
            action.run();
            return null;
        }, actionDescription);
    }

    /**
     * Execute a Playwright action without return value with retry logic
     * @param action The action to execute
     * @param actionDescription Description of the action for logging
     */
    public static void executePlaywrightAction(Runnable action, String actionDescription) {
        executePlaywrightAction(() -> {
            action.run();
            return null;
        }, actionDescription);
    }
}
