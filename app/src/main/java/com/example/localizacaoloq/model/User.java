package com.example.localizacaoloq.model;

public class User {
    private String nome;
    private String password;

    public User(String nome, String password) {
        this.nome = nome;
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }
}
