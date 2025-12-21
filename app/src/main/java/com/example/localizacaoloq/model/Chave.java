package com.example.localizacaoloq.model;

public class Chave {
    private String nome;
    private  String _id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public  Chave(){}
    public  Chave(String nome){
        this.nome=nome;
    }

}
