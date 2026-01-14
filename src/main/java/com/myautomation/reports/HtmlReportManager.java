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
            logger.info("Created report directories: {}", REPORT_DIR);
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
            writer.write("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("\n=== Test Logs ===\n");

            if (logContent != null && !logContent.trim().isEmpty()) {
                writer.write(logContent);
            } else {
                writer.write("No logs captured\n");
            }

            // Add any exception if present
            if (result.getThrowable() != null) {
                writer.write("\n=== Exception Details ===\n");
                writer.write("Error: " + result.getThrowable().getMessage() + "\n\n");

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                result.getThrowable().printStackTrace(pw);
                writer.write(sw.toString());
            }

            logger.info("Saved test log to: {}", logFilePath);
        } catch (IOException e) {
            logger.error("Failed to write test log: {}", e.getMessage());
        }

        // Get relative path for screenshot if it exists
        String relativeScreenshotPath = "";
        if (screenshotPath != null && !screenshotPath.isEmpty()) {
            File screenshotFile = new File(screenshotPath);
            if (screenshotFile.exists()) {
                relativeScreenshotPath = "screenshots/" + screenshotFile.getName();
                logger.info("Screenshot will be referenced at: {}", relativeScreenshotPath);
            } else {
                logger.warn("Screenshot file not found: {}", screenshotPath);
            }
        }

        tests.add(new TestEntry(
                testName,
                status,
                duration,
                relativeScreenshotPath,
                "logs/" + logFileName
        ));
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

            File reportFile = new File(REPORT_FILE);
            logger.info("========================================");
            logger.info("✅ HTML Report generated successfully!");
            logger.info("📁 Location: {}", reportFile.getAbsolutePath());
            logger.info("🌐 Open in browser: file://{}", reportFile.getAbsolutePath());
            logger.info("========================================");

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
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }

    private static String getHeader() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Automation Test Report</title>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        background: #f5f5f5;
                        padding: 20px;
                    }
                    .container {
                        max-width: 1400px;
                        margin: 0 auto;
                        background: white;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 { 
                        color: #2c3e50; 
                        border-bottom: 3px solid #3498db; 
                        padding-bottom: 15px;
                        margin-bottom: 20px;
                        font-size: 32px;
                    }
                    .timestamp { 
                        color: #7f8c8d; 
                        font-size: 14px;
                        margin-bottom: 30px;
                    }
                    .summary { 
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 25px; 
                        border-radius: 8px; 
                        margin: 20px 0;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .summary h2 {
                        color: white;
                        margin-bottom: 15px;
                        font-size: 24px;
                    }
                    .summary-item { 
                        display: inline-block; 
                        margin-right: 30px; 
                        font-size: 16px;
                        background: rgba(255,255,255,0.2);
                        padding: 10px 15px;
                        border-radius: 5px;
                        margin-bottom: 10px;
                    }
                    .summary-value { 
                        font-weight: bold; 
                        font-size: 24px;
                        display: block;
                        margin-top: 5px;
                    }
                    table { 
                        width: 100%; 
                        border-collapse: collapse; 
                        margin: 20px 0; 
                        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        background: white;
                    }
                    th, td { 
                        border: 1px solid #ddd; 
                        padding: 15px; 
                        text-align: left;
                        vertical-align: top;
                    }
                    th { 
                        background: #34495e;
                        color: white;
                        font-weight: 600;
                        position: sticky; 
                        top: 0;
                        z-index: 10;
                    }
                    tr:nth-child(even) { background-color: #f8f9fa; }
                    tr:hover { background-color: #e9ecef; }
                    
                    .PASS { color: #27ae60; font-weight: bold; font-size: 16px; }
                    .FAIL { color: #e74c3c; font-weight: bold; font-size: 16px; }
                    .SKIP { color: #f39c12; font-weight: bold; font-size: 16px; }
                    
                    .PASS-bg { background-color: #d5f4e6 !important; }
                    .FAIL-bg { background-color: #fadbd8 !important; }
                    .SKIP-bg { background-color: #fcf3cf !important; }
                    
                    .screenshot { 
                        max-width: 400px;
                        max-height: 300px;
                        border: 2px solid #ddd; 
                        border-radius: 5px;
                        cursor: pointer;
                        transition: transform 0.2s;
                    }
                    .screenshot:hover {
                        transform: scale(1.05);
                        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
                    }
                    .screenshot-container { 
                        text-align: center; 
                        margin: 10px 0;
                    }
                    .no-screenshot {
                        color: #95a5a6;
                        font-style: italic;
                    }
                    .log-content { 
                        max-height: 300px; 
                        overflow-y: auto; 
                        background: #2c3e50;
                        color: #ecf0f1;
                        padding: 15px; 
                        border: 1px solid #34495e; 
                        border-radius: 5px; 
                        font-family: 'Courier New', monospace;
                        font-size: 12px;
                        white-space: pre-wrap;
                        word-wrap: break-word;
                    }
                    .log-content::-webkit-scrollbar {
                        width: 8px;
                    }
                    .log-content::-webkit-scrollbar-track {
                        background: #34495e;
                    }
                    .log-content::-webkit-scrollbar-thumb {
                        background: #7f8c8d;
                        border-radius: 4px;
                    }
                    .test-name { 
                        font-weight: 600;
                        font-size: 15px;
                        color: #2c3e50;
                    }
                    .duration {
                        color: #7f8c8d;
                        font-family: monospace;
                    }
                    h2 { 
                        color: #2c3e50; 
                        margin-top: 30px;
                        margin-bottom: 15px;
                        font-size: 24px;
                    }
                    
                    /* Modal for full-size screenshots */
                    .modal {
                        display: none;
                        position: fixed;
                        z-index: 1000;
                        left: 0;
                        top: 0;
                        width: 100%;
                        height: 100%;
                        background-color: rgba(0,0,0,0.9);
                    }
                    .modal-content {
                        margin: auto;
                        display: block;
                        max-width: 90%;
                        max-height: 90%;
                        margin-top: 50px;
                    }
                    .close {
                        position: absolute;
                        top: 15px;
                        right: 35px;
                        color: #f1f1f1;
                        font-size: 40px;
                        font-weight: bold;
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🚀 Automation Test Report</h1>
                    <div class="timestamp">Generated on: """ + timestamp + """
</div>
            """;
    }

    private static String getSummary() {
        long pass = tests.stream().filter(t -> t.status.equals("PASS")).count();
        long fail = tests.stream().filter(t -> t.status.equals("FAIL")).count();
        long skip = tests.stream().filter(t -> t.status.equals("SKIP")).count();
        long total = tests.size();
        double passRate = total > 0 ? (pass * 100.0 / total) : 0;

        return """
        <div class="summary">
            <h2>📊 Test Execution Summary</h2>
            <div>
                <span class="summary-item">
                    Total Tests
                    <span class="summary-value">%d</span>
                </span>
                <span class="summary-item">
                    ✅ Passed
                    <span class="summary-value">%d</span>
                </span>
                <span class="summary-item">
                    ❌ Failed
                    <span class="summary-value">%d</span>
                </span>
                <span class="summary-item">
                    ⚠️ Skipped
                    <span class="summary-value">%d</span>
                </span>
                <span class="summary-item">
                    Pass Rate
                    <span class="summary-value">%.1f%%</span>
                </span>
            </div>
        </div>
        """.formatted(total, pass, fail, skip, passRate);
    }

    private static String getTableHeader() {
        return """
        <h2>📋 Detailed Test Results</h2>
        <table>
            <thead>
                <tr>
                    <th style="width: 20%;">Test Name</th>
                    <th style="width: 8%;">Status</th>
                    <th style="width: 10%;">Duration</th>
                    <th style="width: 25%;">Screenshot</th>
                    <th style="width: 37%;">Logs</th>
                </tr>
            </thead>
            <tbody>
        """;
    }

    private static String getRow(TestEntry t) {
        String rowClass = t.status + "-bg";

        // Screenshot cell
        String screenshotCell;
        if (t.screenshotPath != null && !t.screenshotPath.isEmpty()) {
            screenshotCell = String.format(
                    "<div class='screenshot-container'>" +
                            "<a href='%s' target='_blank' onclick='openModal(\"%s\"); return false;'>" +
                            "<img src='%s' class='screenshot' alt='Test Screenshot' title='Click to enlarge'>" +
                            "</a></div>",
                    t.screenshotPath, t.screenshotPath, t.screenshotPath
            );
        } else {
            screenshotCell = "<span class='no-screenshot'>No screenshot available</span>";
        }

        // Log content
        String logContent = "";
        try {
            String logFilePath = REPORT_DIR + "/" + t.logPath;
            Path logPath = Paths.get(logFilePath);
            if (Files.exists(logPath)) {
                logContent = Files.readString(logPath);
            } else {
                logContent = "Log file not found: " + logFilePath;
            }
        } catch (IOException e) {
            logContent = "Failed to load log content: " + e.getMessage();
        }

        // HTML escape the log content
        String escapedLogContent = logContent
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");

        return String.format(
                "<tr class='%s'>" +
                        "<td class='test-name'>%s</td>" +
                        "<td><span class='%s'>%s</span></td>" +
                        "<td class='duration'>%s</td>" +
                        "<td>%s</td>" +
                        "<td><div class='log-content'>%s</div></td>" +
                        "</tr>\n",
                rowClass,
                t.testName,
                t.status,
                t.status,
                t.duration,
                screenshotCell,
                escapedLogContent
        );
    }

    private static String getFooter() {
        return """
            </tbody>
        </table>
        
        <!-- Modal for full-size screenshots -->
        <div id="screenshotModal" class="modal" onclick="closeModal()">
            <span class="close" onclick="closeModal()">&times;</span>
            <img class="modal-content" id="modalImage">
        </div>
        
        <script>
            function openModal(src) {
                document.getElementById('screenshotModal').style.display = 'block';
                document.getElementById('modalImage').src = src;
            }
            
            function closeModal() {
                document.getElementById('screenshotModal').style.display = 'none';
            }
            
            // Close modal on ESC key
            document.addEventListener('keydown', function(event) {
                if (event.key === 'Escape') {
                    closeModal();
                }
            });
        </script>
        
        <div style="text-align: center; margin-top: 40px; padding: 20px; color: #7f8c8d; border-top: 1px solid #ddd;">
            <p>Report generated by Custom HTML Reporter</p>
            <p style="font-size: 12px; margin-top: 5px;">""" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                """
    </p>
            </div>
            
        </div>
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