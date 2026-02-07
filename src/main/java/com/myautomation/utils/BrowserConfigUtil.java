package com.myautomation.utils;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for automatically detecting system configuration
 * for browser context settings
 */
public class BrowserConfigUtil {
    
    /**
     * Gets default viewport size
     */
    public static int[] getOptimalViewportSize() {
        return new int[]{1920, 1080};
    }
    
    /**
     * Gets system locale for browser context
     */
    public static String getSystemLocale() {
        Locale systemLocale = Locale.getDefault();
        String localeString = systemLocale.toLanguageTag();
        
        // Fallback to en-US if unsupported locale
        if (!localeString.matches("en-[A-Z]{2}|es-[A-Z]{2}|fr-[A-Z]{2}|de-[A-Z]{2}|it-[A-Z]{2}|pt-[A-Z]{2}|ja-[A-Z]{2}|zh-[A-Z]{2}")) {
            localeString = "en-US";
        }
        
        return localeString;
    }
    
    /**
     * Gets system timezone for browser context
     */
    public static String getSystemTimezone() {
        TimeZone timeZone = TimeZone.getDefault();
        String timezoneId = timeZone.getID();
        
        // Validate timezone ID (common browser-supported timezones)
        String[] supportedTimezones = {
            "America/New_York", "America/Los_Angeles", "America/Chicago",
            "Europe/London", "Europe/Paris", "Europe/Berlin",
            "Asia/Tokyo", "Asia/Shanghai", "Asia/Dubai",
            "Australia/Sydney", "UTC"
        };
        
        boolean isSupported = false;
        for (String supported : supportedTimezones) {
            if (supported.equals(timezoneId)) {
                isSupported = true;
                break;
            }
        }
        
        if (!isSupported) {
            // Map common timezone IDs to supported ones
            String mappedTimezone = mapTimezone(timezoneId);
            if (mappedTimezone != null) {
                timezoneId = mappedTimezone;
            } else {
                timezoneId = "UTC";
            }
        }
        
        return timezoneId;
    }
    
    /**
     * Maps unsupported timezone IDs to supported ones
     */
    private static String mapTimezone(String timezoneId) {
        // Eastern US timezones
        if (timezoneId.startsWith("US/Eastern") || timezoneId.equals("America/New_York")) {
            return "America/New_York";
        }
        // Pacific US timezones
        if (timezoneId.startsWith("US/Pacific") || timezoneId.equals("America/Los_Angeles")) {
            return "America/Los_Angeles";
        }
        // Central US timezones
        if (timezoneId.startsWith("US/Central") || timezoneId.equals("America/Chicago")) {
            return "America/Chicago";
        }
        // UK timezones
        if (timezoneId.startsWith("Europe/London") || timezoneId.equals("GMT")) {
            return "Europe/London";
        }
        // Paris timezones
        if (timezoneId.startsWith("Europe/Paris") || timezoneId.equals("CET")) {
            return "Europe/Paris";
        }
        // Berlin timezones
        if (timezoneId.startsWith("Europe/Berlin")) {
            return "Europe/Berlin";
        }
        // Tokyo timezones
        if (timezoneId.startsWith("Asia/Tokyo") || timezoneId.equals("JST")) {
            return "Asia/Tokyo";
        }
        // Shanghai timezones
        if (timezoneId.startsWith("Asia/Shanghai") || timezoneId.equals("CST")) {
            return "Asia/Shanghai";
        }
        
        return null;
    }
    
    /**
     * Detects if HTTPS errors should be ignored based on environment
     */
    public static boolean shouldIgnoreHTTPSErrors() {
        // In development/testing environments, ignore HTTPS errors
        String env = System.getProperty("environment", System.getenv().getOrDefault("ENVIRONMENT", "development"));
        boolean ignoreErrors = env.equalsIgnoreCase("development") || env.equalsIgnoreCase("test");
        
        return ignoreErrors;
    }
    
    /**
     * Gets user agent string based on browser type
     */
    public static String getUserAgent(String browserType) {
        String os = System.getProperty("os.name").toLowerCase();
        String userAgent;
        
        if (os.contains("win")) {
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        } else if (os.contains("mac")) {
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        } else {
            userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        }
        
        return userAgent;
    }
}
