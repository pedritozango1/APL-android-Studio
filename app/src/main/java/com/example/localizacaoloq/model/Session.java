package com.example.localizacaoloq.model;

public class Session {
    private User user;
    private String sessionId;
    private boolean active;
    public Session() {
    }
    public Session(User user, String sessionId, boolean active) {
        this.user = user;
        this.sessionId = sessionId;
        this.active = active;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
