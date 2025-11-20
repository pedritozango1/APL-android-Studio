package com.example.localizacaoloq.model;

public class Anuncio {
    public String titulo;
    public String descricao;
    public String local;
    public String data;
    public String tag;

    public Anuncio(String titulo, String descricao, String local, String data, String tag) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.local = local;
        this.data = data;
        this.tag = tag;
    }
}
