package com.example.localizacaoloq.model;

import java.util.List;

public class LocalGPS extends Local {
    private double latitude; // Corrigido de "latutude"
    private double longitude;
    private double altitude;
    private double raio;

    public LocalGPS(String nome, double latitude, double longitude, double altitude, double raio) {
        super(nome, "GPS");
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.raio = raio;
    }

    // Getters e Setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {
        this.raio = raio;
    }

    @Override
    public String toString() {
        return String.format("%s (GPS: %.4f, %.4f, %.0fm)", getNome(), latitude, longitude, raio);
    }
}