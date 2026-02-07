package com.myautomation.sessions;

/**
 * Thread-safe holder for PlaywrightCucumberSession instance
 * This allows sharing the same session between hooks and steps
 */
public class PlaywrightSessionHolder {
    
    private static final ThreadLocal<PlaywrightCucumberSession> sessionHolder = new ThreadLocal<>();
    
    public static void setSession(PlaywrightCucumberSession session) {
        sessionHolder.set(session);
    }
    
    public static PlaywrightCucumberSession getSession() {
        return sessionHolder.get();
    }
    
    public static void clearSession() {
        sessionHolder.remove();
    }
}
