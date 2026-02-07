package com.myautomation.reporting;

import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ReportCustomizer {

    public static void inject(ExtentSparkReporter spark) {

        spark.config().setCss(
                ".log-block { " +
                "  font-family: monospace; " +
                "  background: #f4f4f4; " +
                "  padding: 8px; " +
                "  margin: 4px 0; " +
                "  border-left: 4px solid #3498db; " +
                "} " +

                ".toggle-btn { " +
                "  margin: 10px 0; " +
                "  padding: 6px 12px; " +
                "  background: #3498db; " +
                "  color: white; " +
                "  border: none; " +
                "  cursor: pointer; " +
                "} "
        );

        spark.config().setJs(
                "document.addEventListener('DOMContentLoaded', function() {" +
                "  document.querySelectorAll('.test-content').forEach(function(test) {" +
                "    let btn = document.createElement('button');" +
                "    btn.innerHTML = 'Show / Hide Logs';" +
                "    btn.className = 'toggle-btn';" +
                "    btn.onclick = function() {" +
                "      test.querySelectorAll('.log').forEach(l => {" +
                "        l.style.display = l.style.display === 'none' ? 'block' : 'none';" +
                "      });" +
                "    };" +
                "    test.prepend(btn);" +
                "  });" +
                "});"
        );
    }
}
