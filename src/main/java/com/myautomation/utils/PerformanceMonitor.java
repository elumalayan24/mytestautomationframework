package com.myautomation.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.text.DecimalFormat;

public class PerformanceMonitor {
    private static final ConcurrentHashMap<String, AtomicLong> metrics = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();
    private static final DecimalFormat df = new DecimalFormat("#.##");
    
    public static void startTimer(String operation) {
        startTimes.put(operation, System.currentTimeMillis());
    }
    
    public static void endTimer(String operation) {
        Long startTime = startTimes.remove(operation);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            metrics.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(duration);
            System.out.println(String.format("[PERF] %s: %dms", operation, duration));
        }
    }
    
    public static void incrementCounter(String metric) {
        metrics.computeIfAbsent(metric, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public static void addMetric(String metric, long value) {
        metrics.computeIfAbsent(metric, k -> new AtomicLong(0)).addAndGet(value);
    }
    
    public static void printReport() {
        System.out.println("\n=== PERFORMANCE REPORT ===");
        
        // Time-based metrics
        System.out.println("\nTiming Metrics:");
        metrics.entrySet().stream()
            .filter(e -> e.getKey().contains("Time") || e.getKey().contains("Duration"))
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
            .forEach(e -> System.out.println(String.format("  %s: %dms (%.2fs)", 
                e.getKey(), e.getValue().get(), e.getValue().get() / 1000.0)));
        
        // Count-based metrics
        System.out.println("\nCount Metrics:");
        metrics.entrySet().stream()
            .filter(e -> !e.getKey().contains("Time") && !e.getKey().contains("Duration"))
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
            .forEach(e -> System.out.println(String.format("  %s: %d", e.getKey(), e.getValue().get())));
        
        System.out.println("========================\n");
    }
    
    public static long getMetric(String metric) {
        AtomicLong value = metrics.get(metric);
        return value != null ? value.get() : 0;
    }
    
    public static void reset() {
        metrics.clear();
        startTimes.clear();
    }
}
