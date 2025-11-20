package com.example.localizacaoloq.model;

public class Atributo {
    private String key;
    private String value;

    public Atributo(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
