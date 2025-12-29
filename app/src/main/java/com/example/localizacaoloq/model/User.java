package com.example.localizacaoloq.model;

import java.util.ArrayList;

public class User {
    private String username;
    private String _id;
    public  User(){}
    private ArrayList<Perfil> perfil=new ArrayList<>();

    public ArrayList<Perfil> getPerfil() {
        return perfil;
    }

    public void addPerfil(Perfil perfil) {
        this.perfil.add(perfil);
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
