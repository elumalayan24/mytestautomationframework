package com.myautomation.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionLogger {
    private final String logFileName;
    private static final String LOG_DIR = "logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final BufferedWriter writer;
    private final String sessionId;

    public SessionLogger(String testName) {
        // Generate a more unique session ID with test name, timestamp, and random component
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String randomSuffix = String.format("%04d", (int)(Math.random() * 10000));
        this.sessionId = String.format("TestSuite_%s_%s_%s", 
            testName.replaceAll("[^a-zA-Z0-9]", ""), 
            timestamp,
            randomSuffix);
            
        // Create log file name with test name and timestamp
        this.logFileName = String.format("%s/%s_%s.log", LOG_DIR, testName, timestamp);
        
        // Create logs directory if it doesn't exist
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        
        try {
            this.writer = new BufferedWriter(new FileWriter(logFileName, true));
            log("Session started: " + sessionId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logger", e);
        }
    }

    /**
     * Logs a message with the current session context
     * @param message The message to log
     */
    public void log(String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] [%s] %s%n", timestamp, sessionId, message);
        
        try {
            writer.write(logMessage);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
        
        // Also print to console
        System.out.print(logMessage);
    }

    /**
     * Gets the current session ID.
     * @return The unique session ID for this test run
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the current test name from the session ID
     * @return The test name portion of the session ID
     */
    public String getTestName() {
        return sessionId.replaceAll("^TestSuite_([^_]+).*", "$1");
    }

    public void close() {
        try {
            log("Session ended: " + sessionId);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error closing logger: " + e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
