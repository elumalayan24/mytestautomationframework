package com.myautomation.utils;

/**
 * Utility class for common assertions and validations.
 * Provides methods to validate conditions and throw appropriate exceptions when they fail.
 */
public final class Assertions {
    private static final String DEFAULT_MESSAGE = "Assertion failed";

    private Assertions() {
        // Private constructor to prevent instantiation
    }

    // Null checks
    
    /**
     * Asserts that the specified object is not null.
     *
     * @param object  the object to check
     * @param message the exception message if the assertion fails
     * @throws IllegalArgumentException if the object is null
     */
    public static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            String errorMsg = message != null ? message : "Object cannot be null";
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
        return object;
    }

    /**
     * Asserts that the specified object is not null.
     *
     * @param object the object to check
     * @throws IllegalArgumentException if the object is null
     */
    public static <T> T requireNonNull(T object) {
        return requireNonNull(object, DEFAULT_MESSAGE);
    }

    // String validations
    
    /**
     * Asserts that the specified string is not null or empty.
     *
     * @param str     the string to check
     * @param message the exception message if the assertion fails
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String requireNonEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            String errorMsg = message != null ? message : "String cannot be null or empty";
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
        return str;
    }

    /**
     * Asserts that the specified string is not null or empty.
     *
     * @param str the string to check
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String requireNonEmpty(String str) {
        return requireNonEmpty(str, DEFAULT_MESSAGE);
    }

    // Boolean conditions
    
    /**
     * Asserts that the specified condition is true.
     *
     * @param condition the condition to check
     * @param message   the exception message if the assertion fails
     * @throws IllegalStateException if the condition is false
     */
    public static void checkState(boolean condition, String message) {
        if (!condition) {
            String errorMsg = message != null ? message : "Illegal state";
            ErrorLogger.error(errorMsg, new IllegalStateException(errorMsg));
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * Asserts that the specified condition is true.
     *
     * @param condition the condition to check
     * @throws IllegalStateException if the condition is false
     */
    public static void checkState(boolean condition) {
        checkState(condition, DEFAULT_MESSAGE);
    }

    // Numeric validations
    
    /**
     * Asserts that the specified number is positive.
     *
     * @param number  the number to check
     * @param message the exception message if the assertion fails
     * @throws IllegalArgumentException if the number is not positive
     */
    public static void requirePositive(Number number, String message) {
        if (number == null || number.doubleValue() <= 0) {
            String errorMsg = message != null ? message : "Number must be positive";
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Asserts that the specified number is positive.
     *
     * @param number the number to check
     * @throws IllegalArgumentException if the number is not positive
     */
    public static void requirePositive(Number number) {
        requirePositive(number, DEFAULT_MESSAGE);
    }

    /**
     * Asserts that the specified number is not negative.
     *
     * @param number  the number to check
     * @param message the exception message if the assertion fails
     * @throws IllegalArgumentException if the number is negative
     */
    public static void requireNonNegative(Number number, String message) {
        if (number == null || number.doubleValue() < 0) {
            String errorMsg = message != null ? message : "Number must not be negative";
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Asserts that the specified number is not negative.
     *
     * @param number the number to check
     * @throws IllegalArgumentException if the number is negative
     */
    public static void requireNonNegative(Number number) {
        requireNonNegative(number, DEFAULT_MESSAGE);
    }

    // Array and collection validations
    
    /**
     * Asserts that the specified array is not null or empty.
     *
     * @param array   the array to check
     * @param message the exception message if the assertion fails
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static <T> T[] requireNonEmpty(T[] array, String message) {
        if (array == null || array.length == 0) {
            String errorMsg = message != null ? message : "Array cannot be null or empty";
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
        return array;
    }

    /**
     * Asserts that the specified array is not null or empty.
     *
     * @param array the array to check
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static <T> T[] requireNonEmpty(T[] array) {
        return requireNonEmpty(array, DEFAULT_MESSAGE);
    }

    // Range validations
    
    /**
     * Asserts that the specified value is within the given range [min, max].
     *
     * @param value   the value to check
     * @param min     the minimum allowed value (inclusive)
     * @param max     the maximum allowed value (inclusive)
     * @param message the exception message if the assertion fails
     * @throws IllegalArgumentException if the value is out of range
     */
    public static void requireInRange(int value, int min, int max, String message) {
        if (value < min || value > max) {
            String errorMsg = message != null ? message : 
                String.format("Value %d is not in range [%d, %d]", value, min, max);
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Asserts that the specified value is within the given range [min, max].
     *
     * @param value the value to check
     * @param min   the minimum allowed value (inclusive)
     * @param max   the maximum allowed value (inclusive)
     * @throws IllegalArgumentException if the value is out of range
     */
    public static void requireInRange(int value, int min, int max) {
        requireInRange(value, min, max, DEFAULT_MESSAGE);
    }

    // Custom validation with predicate
    
    /**
     * Validates the specified object using the provided predicate.
     *
     * @param <T>       the type of the object to validate
     * @param object    the object to validate
     * @param predicate the predicate to test the object
     * @param message   the exception message if validation fails
     * @throws IllegalArgumentException if the object doesn't pass the predicate test
     */
    public static <T> void validate(T object, java.util.function.Predicate<T> predicate, String message) {
        if (!predicate.test(object)) {
            String errorMsg = message != null ? message : "Validation failed";
            ErrorLogger.error(errorMsg, new IllegalArgumentException(errorMsg));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    // Custom validation with custom exception
    
    /**
     * Validates a condition and throws the specified exception if the condition is false.
     *
     * @param <X>       the type of the exception to throw
     * @param condition the condition to check
     * @param exceptionSupplier the supplier of the exception to throw
     * @throws X if the condition is false
     */
    public static <X extends Throwable> void check(boolean condition, java.util.function.Supplier<? extends X> exceptionSupplier) throws X {
        if (!condition) {
            X exception = exceptionSupplier.get();
            ErrorLogger.error(exception.getMessage(), exception);
            throw exception;
        }
    }
}
