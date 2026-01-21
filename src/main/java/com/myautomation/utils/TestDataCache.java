package com.myautomation.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TestDataCache {
    private static final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long DEFAULT_TTL = 300000; // 5 minutes in milliseconds
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> type) {
        Object value = cache.get(key);
        if (value != null && !isExpired(key)) {
            return (T) value;
        }
        return null;
    }
    
    public static <T> T getOrCompute(String key, Supplier<T> supplier, Class<T> type) {
        T value = get(key, type);
        if (value == null) {
            value = supplier.get();
            put(key, value);
        }
        return value;
    }
    
    public static void put(String key, Object value) {
        cache.put(key, value);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    public static void put(String key, Object value, long ttlMillis) {
        cache.put(key, value);
        cacheTimestamps.put(key, System.currentTimeMillis() + ttlMillis);
    }
    
    public static void remove(String key) {
        cache.remove(key);
        cacheTimestamps.remove(key);
    }
    
    public static void clear() {
        cache.clear();
        cacheTimestamps.clear();
    }
    
    public static void clearExpired() {
        cacheTimestamps.entrySet().removeIf(entry -> {
            if (isExpired(entry.getKey())) {
                cache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    private static boolean isExpired(String key) {
        Long timestamp = cacheTimestamps.get(key);
        return timestamp == null || System.currentTimeMillis() > timestamp;
    }
    
    public static int size() {
        clearExpired();
        return cache.size();
    }
    
    public static Map<String, Object> getSnapshot() {
        clearExpired();
        return new ConcurrentHashMap<>(cache);
    }
}
