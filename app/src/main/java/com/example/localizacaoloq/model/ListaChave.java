package com.example.localizacaoloq.model;

public class ListaChave {
    private Chave chave;
    private String valor;

    public String getValor() {
        return valor;
    }
public  ListaChave(Chave chave,String valor){
        this.chave=chave;
        this.valor=valor;
}
    public void setValor(String valor) {
        this.valor = valor;
    }

    public Chave getChave() {
        return chave;
    }

    public void setChave(Chave chave) {
        this.chave = chave;
    }
}
