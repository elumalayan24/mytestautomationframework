package main.java.com.myautomation.session;

import com.myautomation.session.Session;

import java.time.Instant;

/**
 * Default implementation of the Session interface.
 */
public class DefaultSession implements Session {
    private final String id;
    private volatile boolean valid = true;
    private final Instant creationTime;
    private volatile Instant lastAccessedTime;
    private int maxInactiveInterval = 1800; // 30 minutes default

    public DefaultSession(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        this.id = id;
        this.creationTime = Instant.now();
        this.lastAccessedTime = this.creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        return valid && !isExpired();
    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public long getCreationTime() {
        return creationTime.toEpochMilli();
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime.toEpochMilli();
    }

    @Override
    public void updateLastAccessedTime() {
        this.lastAccessedTime = Instant.now();
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval > 0 ? interval : 0;
    }

    private boolean isExpired() {
        if (maxInactiveInterval <= 0) {
            return false; // Never expires
        }
        return Instant.now().isAfter(lastAccessedTime.plusSeconds(maxInactiveInterval));
    }
}
