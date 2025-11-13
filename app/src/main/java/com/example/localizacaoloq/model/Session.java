package com.example.localizacaoloq.model;

public class Session {
    private String user;
    private String sessionId;
    private boolean active;
    public Session() {
    }
    public Session(String user, String sessionId, boolean active) {
        this.user = user;
        this.sessionId = sessionId;
        this.active = active;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
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
