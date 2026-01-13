package com.myautomation.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.nio.file.*;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlReportManager {
    private static final Logger logger = LoggerFactory.getLogger(HtmlReportManager.class);
    private static final List<TestEntry> tests = new ArrayList<>();
    private static final String TIMESTAMP = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    public static final String REPORT_DIR = "test-output/html-report/" + TIMESTAMP;
    private static final String SCREENSHOT_DIR = REPORT_DIR + "/screenshots";
    private static final String LOG_DIR = REPORT_DIR + "/logs";
    private static final String REPORT_FILE = REPORT_DIR + "/AutomationReport.html";

    static {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            logger.error("Failed to create report directories: {}", e.getMessage());
        }
    }

    public static void logTest(ITestResult result, String screenshotPath, String logContent) {
        String testName = result.getMethod().getMethodName();
        String status = getStatus(result.getStatus());
        String duration = formatDuration(result.getEndMillis() - result.getStartMillis());
        
        // Save log to file
        String logFileName = testName + "_" + System.currentTimeMillis() + ".log";
        String logFilePath = LOG_DIR + "/" + logFileName;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath))) {
            writer.write("Test: " + testName + "\n");
            writer.write("Status: " + status + "\n");
            writer.write("Duration: " + duration + "\n");
            writer.write("\n--- Test Logs ---\n");
            writer.write(logContent);
            
            // Add any exception if present
            if (result.getThrowable() != null) {
                writer.write("\n--- Exception ---\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                result.getThrowable().printStackTrace(pw);
                writer.write(sw.toString());
            }
        } catch (IOException e) {
            logger.error("Failed to write test log: {}", e.getMessage());
        }
        
        tests.add(new TestEntry(testName, status, duration, 
                screenshotPath != null ? "screenshots/" + new File(screenshotPath).getName() : "",
                "logs/" + logFileName));
    }

    public static void generateReport() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE))) {
            writer.write(getHeader());
            writer.write(getSummary());
            writer.write(getTableHeader());

            for (TestEntry t : tests) {
                writer.write(getRow(t));
            }

            writer.write(getFooter());
            logger.info("✅ HTML Report generated: {}", new File(REPORT_FILE).getAbsolutePath());

        } catch (IOException e) {
            logger.error("Failed to generate HTML report: {}", e.getMessage());
        }
    }

    private static String getStatus(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            case ITestResult.SKIP -> "SKIP";
            default -> "UNKNOWN";
        };
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static String getHeader() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "<!DOCTYPE html>" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Automation Test Report</title>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <style>\n" +
            "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 1200px; margin: 0 auto; padding: 20px; }\n" +
            "        table { width: 100%; border-collapse: collapse; margin: 20px 0; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }\n" +
            "        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n" +
            "        th { background-color: #f8f9fa; position: sticky; top: 0; }\n" +
            "        tr:nth-child(even) { background-color: #f9f9f9; }\n" +
            "        tr:hover { background-color: #f1f1f1; }\n" +
            "        .PASS { color: #28a745; font-weight: bold; }\n" +
            "        .FAIL { color: #dc3545; font-weight: bold; }\n" +
            "        .SKIP { color: #ffc107; font-weight: bold; }\n" +
            "        .screenshot { max-width: 300px; border: 1px solid #ddd; border-radius: 4px; }\n" +
            "        .summary { background: #f8f9fa; padding: 15px; border-radius: 4px; margin: 20px 0; }\n" +
            "        .summary-item { display: inline-block; margin-right: 20px; font-size: 16px; }\n" +
            "        .summary-value { font-weight: bold; font-size: 18px; }\n" +
            "        .PASS-bg { background-color: #d4edda; }\n" +
            "        .FAIL-bg { background-color: #f8d7da; }\n" +
            "        .SKIP-bg { background-color: #fff3cd; }\n" +
            "        .log-content { max-height: 200px; overflow-y: auto; background: #f8f9fa; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-family: monospace; white-space: pre-wrap; }\n" +
            "        .test-name { font-weight: 600; }\n" +
            "        .timestamp { color: #6c757d; font-size: 0.9em; }\n" +
            "        h1 { color: #343a40; border-bottom: 2px solid #eee; padding-bottom: 10px; }\n" +
            "        h2 { color: #495057; margin-top: 30px; }\n" +
            "        .screenshot-container { text-align: center; margin: 10px 0; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>Automation Execution Report</h1>\n" +
            "    <div class=\"timestamp\">Generated on: " + timestamp + "</div>\n";
    }

    private static String getSummary() {
        long pass = tests.stream().filter(t -> t.status.equals("PASS")).count();
        long fail = tests.stream().filter(t -> t.status.equals("FAIL")).count();
        long skip = tests.stream().filter(t -> t.status.equals("SKIP")).count();
        long total = tests.size();
        
        return """
        <div class="summary">
            <h2>Test Summary</h2>
            <div>
                <span class="summary-item">Total Tests: <span class="summary-value">%d</span></span>
                <span class="summary-item">Passed: <span class="summary-value" style="color: #28a745">%d</span></span>
                <span class="summary-item">Failed: <span class="summary-value" style="color: #dc3545">%d</span></span>
                <span class="summary-item">Skipped: <span class="summary-value" style="color: #ffc107">%d</span></span>
                <span class="summary-item">Pass Rate: <span class="summary-value">%.2f%%</span></span>
            </div>
        </div>
        """.formatted(total, pass, fail, skip, total > 0 ? (pass * 100.0 / total) : 0);
    }

    private static String getTableHeader() {
        return """
        <h2>Test Results</h2>
        <table>
            <thead>
                <tr>
                    <th>Test Name</th>
                    <th>Status</th>
                    <th>Duration</th>
                    <th>Screenshot</th>
                    <th>Logs</th>
                </tr>
            </thead>
            <tbody>
        """;
    }

    private static String getRow(TestEntry t) {
        String rowClass = t.status + "-bg";
        String screenshotCell = t.screenshotPath.isEmpty() 
            ? "No screenshot" 
            : String.format("<div class='screenshot-container'><a href='%s' target='_blank'><img src='%s' class='screenshot' alt='Screenshot'></a></div>", 
                          t.screenshotPath, t.screenshotPath);
                          
        String logContent = "";
        try {
            String logFilePath = REPORT_DIR + "/" + t.logPath;
            logContent = new String(Files.readAllBytes(Paths.get(logFilePath)));
        } catch (IOException e) {
            logContent = "Failed to load log content: " + e.getMessage();
        }
        
        return String.format(
            "<tr class='%s'>" +
            "<td class='test-name'>%s</td>" +
            "<td><span class='%s'>%s</span></td>" +
            "<td>%s</td>" +
            "<td>%s</td>" +
            "<td><div class='log-content'><pre>%s</pre></div></td>" +
            "</tr>",
            rowClass, t.testName, t.status, t.status, t.duration, 
            screenshotCell, logContent.replace("<", "&lt;").replace(">", "&gt;")
        );
    }

    private static String getFooter() {
        return """
            </tbody>
        </table>
        </body>
        </html>
        """;
    }

    private static class TestEntry {
        String testName, status, duration, screenshotPath, logPath;

        TestEntry(String testName, String status, String duration, String screenshotPath, String logPath) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
            this.screenshotPath = screenshotPath;
            this.logPath = logPath;
        }
    }
}
