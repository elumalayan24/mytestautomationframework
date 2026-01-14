package com.myautomation.utils;

import com.microsoft.playwright.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Function;

/**
 * Unified exception handling for both Playwright and Selenium WebDriver.
 * Provides methods to handle common automation exceptions and retry mechanisms.
 */
public final class AutomationExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AutomationExceptionHandler.class);
    private static final int MAX_RETRIES = 3;
    private static final long POLLING_INTERVAL_MS = 500;

    private AutomationExceptionHandler() {
        // Private constructor to prevent instantiation
    }

    // ==================== SELENIUM EXCEPTION HANDLING ====================

    /**
     * Executes a Selenium action with retry logic for common exceptions
     *
     * @param action The Selenium action to perform
     * @param context Description of the action being performed (for logging)
     * @param <T>    Return type of the action
     * @return The result of the action
     * @throws RuntimeException if all retries are exhausted
     */
    public static <T> T executeSeleniumAction(SeleniumAction<T> action, String context) {
        int attempts = 0;
        RuntimeException lastException;

        do {
            try {
                return action.execute();
            } catch (StaleElementReferenceException e) {
                lastException = handleStaleElementException(e, context, attempts);
            } catch (NoSuchElementException e) {
                lastException = handleNoSuchElementException(e, context, attempts);
            } catch (ElementNotInteractableException e) {
                lastException = handleElementNotInteractableException(e, context, attempts);
            } catch (TimeoutException e) {
                lastException = handleTimeoutException(e, context, attempts);
            } catch (WebDriverException e) {
                lastException = handleWebDriverException(e, context, attempts);
            }
            
            attempts++;
            if (attempts < MAX_RETRIES) {
                logger.warn("Retry attempt {}/{} for: {}", attempts, MAX_RETRIES, context);
                waitBeforeRetry(attempts);
            }
        } while (attempts < MAX_RETRIES);

        // Log the final failure
        String errorMsg = String.format("Failed to perform Selenium action after %d attempts: %s", 
                                      MAX_RETRIES, context);
        logger.error(errorMsg, lastException);
        throw new AutomationException(errorMsg, lastException);
    }

    // ==================== PLAYWRIGHT EXCEPTION HANDLING ====================

    /**
     * Executes a Playwright action with retry logic for common exceptions
     *
     * @param action  The Playwright action to perform
     * @param context Description of the action being performed (for logging)
     * @param <T>     Return type of the action
     * @return The result of the action
     * @throws RuntimeException if all retries are exhausted
     */
    public static <T> T executePlaywrightAction(PlaywrightAction<T> action, String context) {
        int attempts = 0;
        RuntimeException lastException;

        do {
            try {
                return action.execute();
            } catch (TimeoutError e) {
                lastException = handlePlaywrightTimeoutError(e, context, attempts);
            } catch (PlaywrightException e) {
                lastException = handlePlaywrightException(e, context, attempts);
            } catch (Error e) {
                // Handle JavaScript errors that might be thrown by Playwright
                lastException = handleJavaScriptError(e, context, attempts);
            }
            
            attempts++;
            if (attempts < MAX_RETRIES) {
                logger.warn("Retry attempt {}/{} for: {}", attempts, MAX_RETRIES, context);
                waitBeforeRetry(attempts);
            }
        } while (attempts < MAX_RETRIES);

        // Log the final failure
        String errorMsg = String.format("Failed to perform Playwright action after %d attempts: %s", 
                                      MAX_RETRIES, context);
        logger.error(errorMsg, lastException);
        throw new AutomationException(errorMsg, lastException);
    }

    // ==================== COMMON UTILITY METHODS ====================

    private static void waitBeforeRetry(int attempt) {
        try {
            // Exponential backoff
            long waitTime = (long) (POLLING_INTERVAL_MS * Math.pow(2, attempt - 1));
            Thread.sleep(Math.min(waitTime, 5000)); // Cap at 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AutomationException("Thread was interrupted while waiting before retry", e);
        }
    }

    // ==================== SELENIUM SPECIFIC HANDLERS ====================

    private static RuntimeException handleStaleElementException(StaleElementReferenceException e, 
                                                             String context, int attempt) {
        String msg = String.format("StaleElementReferenceException in %s (attempt %d/%d)", 
                                 context, attempt + 1, MAX_RETRIES);
        logger.debug(msg, e);
        return e;
    }

    private static RuntimeException handleNoSuchElementException(NoSuchElementException e, 
                                                              String context, int attempt) {
        String msg = String.format("Element not found in %s (attempt %d/%d)", 
                                 context, attempt + 1, MAX_RETRIES);
        logger.warn(msg);
        return e;
    }

    private static RuntimeException handleElementNotInteractableException(ElementNotInteractableException e, 
                                                                       String context, int attempt) {
        String msg = String.format("Element not interactable in %s (attempt %d/%d)", 
                                 context, attempt + 1, MAX_RETRIES);
        logger.warn(msg);
        return e;
    }

    private static RuntimeException handleTimeoutException(TimeoutException e, 
                                                        String context, int attempt) {
        String msg = String.format("Timeout in %s (attempt %d/%d)", 
                                 context, attempt + 1, MAX_RETRIES);
        logger.warn(msg);
        return e;
    }

    private static RuntimeException handleWebDriverException(WebDriverException e, 
                                                          String context, int attempt) {
        String msg = String.format("WebDriver error in %s (attempt %d/%d): %s", 
                                 context, attempt + 1, MAX_RETRIES, e.getMessage());
        logger.warn(msg);
        return e;
    }

    // ==================== PLAYWRIGHT SPECIFIC HANDLERS ====================

    private static RuntimeException handlePlaywrightTimeoutError(TimeoutError e, 
                                                              String context, int attempt) {
        String msg = String.format("Playwright timeout in %s (attempt %d/%d)", 
                                 context, attempt + 1, MAX_RETRIES);
        logger.warn(msg);
        return e;
    }

    private static RuntimeException handlePlaywrightException(PlaywrightException e, 
                                                           String context, int attempt) {
        String msg = String.format("Playwright error in %s (attempt %d/%d): %s", 
                                 context, attempt + 1, MAX_RETRIES, e.getMessage());
        logger.warn(msg);
        return e;
    }

    private static RuntimeException handleJavaScriptError(Error e, String context, int attempt) {
        String msg = String.format("JavaScript error in %s (attempt %d/%d): %s", 
                                 context, attempt + 1, MAX_RETRIES, e.getMessage());
        logger.warn(msg);
        return new AutomationException(msg, e);
    }

    // ==================== FUNCTIONAL INTERFACES ====================

    @FunctionalInterface
    public interface SeleniumAction<T> {
        T execute();
    }

    @FunctionalInterface
    public interface PlaywrightAction<T> {
        T execute();
    }
}

/**
 * Custom exception for automation framework
 */
class AutomationException extends RuntimeException {
    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}
