package com.myautomation.utils;

import com.aventstack.extentreports.ExtentTest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LogCaptureUtil {
    private static final ThreadLocal<ByteArrayOutputStream> logStream = new ThreadLocal<>();
    private static final ThreadLocal<PrintStream> originalOut = new ThreadLocal<>();
    private static final ThreadLocal<PrintStream> originalErr = new ThreadLocal<>();
    private static final ThreadLocal<Queue<String>> logQueue = ThreadLocal.withInitial(LinkedList::new);
    private static final ConcurrentMap<Long, Queue<String>> threadLogs = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void startCapture() {
        try {
            if (logStream.get() != null) {
                return; // Already capturing
            }

            originalOut.set(System.out);
            originalErr.set(System.err);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            logStream.set(baos);

            PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);

            System.setOut(ps);
            System.setErr(ps);

            logQueue.set(new LinkedList<>());
            threadLogs.put(Thread.currentThread().getId(), logQueue.get());

            log("Log capture started");
        } catch (Exception e) {
            System.err.println("Failed to start log capture: " + e.getMessage());
            throw new RuntimeException("Failed to start log capture", e);
        }
    }

    public static String getLogs() {
        try {
            ByteArrayOutputStream baos = logStream.get();
            return baos != null ? baos.toString(StandardCharsets.UTF_8) : "";
        } catch (Exception e) {
            System.err.println("Failed to get logs: " + e.getMessage());
            return "Error retrieving logs: " + e.getMessage();
        }
    }

    public static void log(String message) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] [Thread-%d] %s",
                timestamp, Thread.currentThread().getId(), message);

        System.out.println(logEntry);

        Queue<String> queue = logQueue.get();
        if (queue != null) {
            queue.add(logEntry);
            while (queue.size() > 1000) {
                queue.poll();
            }
        }
    }

    public static void addLogsToReport(ExtentTest test) {
        if (test == null) return;

        try {
            Queue<String> logs = logQueue.get();
            if (logs != null && !logs.isEmpty()) {
                StringBuilder logBuilder = new StringBuilder();
                logBuilder.append("<div style='font-family: monospace; font-size: 12px;'>");
                logs.forEach(log -> logBuilder.append(log).append("<br>"));
                logBuilder.append("</div>");
                test.info(logBuilder.toString());
            }
        } catch (Exception e) {
            System.err.println("Failed to add logs to report: " + e.getMessage());
        }
    }

    public static void stopCapture() {
        try {
            if (originalOut.get() != null) {
                System.setOut(originalOut.get());
            }
            if (originalErr.get() != null) {
                System.setErr(originalErr.get());
            }

            ByteArrayOutputStream baos = logStream.get();
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    System.err.println("Error closing log stream: " + e.getMessage());
                }
            }

            threadLogs.remove(Thread.currentThread().getId());
            logQueue.remove();
            logStream.remove();
            originalOut.remove();
            originalErr.remove();

        } catch (Exception e) {
            System.err.println("Error stopping log capture: " + e.getMessage());
        }
    }
}