package com.example.localizacaoloq.model;

public class Perfil {
    private String _id;
    private String valor;
    private String chave;

    // Construtor vazio
    public Perfil() {}

    // Construtor com parâmetros
    public Perfil(String chave, String valor) {
        this.chave = chave;
        this.valor = valor;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}