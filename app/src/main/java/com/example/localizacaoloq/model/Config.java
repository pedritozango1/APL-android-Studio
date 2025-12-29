package com.example.localizacaoloq.model;

public class Config {
    private int id;
    private String userId;
    private boolean modoMula;
    private boolean wifiDirect;
    private boolean notificacoes;

    // Construtor padrão
    public Config() {}

    // Construtor com parâmetros
    public Config(String userId, boolean modoMula, boolean wifiDirect, boolean notificacoes) {
        this.userId = userId;
        this.modoMula = modoMula;
        this.wifiDirect = wifiDirect;
        this.notificacoes = notificacoes;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isModoMula() { return modoMula; }
    public void setModoMula(boolean modoMula) { this.modoMula = modoMula; }

    public boolean isWifiDirect() { return wifiDirect; }
    public void setWifiDirect(boolean wifiDirect) { this.wifiDirect = wifiDirect; }

    public boolean isNotificacoes() { return notificacoes; }
    public void setNotificacoes(boolean notificacoes) { this.notificacoes = notificacoes; }

    @Override
    public String toString() {
        return "Config{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", modoMula=" + modoMula +
                ", wifiDirect=" + wifiDirect +
                ", notificacoes=" + notificacoes +
                '}';
    }
}