package com.example.localizacaoloq.model;

public abstract class Local {
    private  String _id;
    private String nome;
    private String tipo; // "GPS" ou "WIFI"



    public Local(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", nome, tipo);
    }
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}