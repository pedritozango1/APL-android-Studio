package com.example.localizacaoloq.model;

import java.util.List;

public class LocalWifi extends Local {
    private List<String> sinal;

    public LocalWifi(String nome, List<String> sinal) {
        super(nome, "WIFI");
        this.sinal = sinal;
    }

    // Getters e Setters
    public List<String> getSinal() {
        return sinal;
    }

    public void setSinal(List<String> sinal) {
        this.sinal = sinal;
    }
    public List<String> getWifiIds() {
        return getSinal();
    }
    public void setWifiIds(List<String> wifiIds) {
        setSinal(wifiIds);
    }
    @Override
    public String toString() {
        return String.format("%s (WIFI: %d IDs - %s)", getNome(), sinal.size(), String.join(", ", sinal));
    }
}