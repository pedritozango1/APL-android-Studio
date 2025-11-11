package com.example.localizacaoloq.model;

import android.app.Application;
import java.util.ArrayList;
import java.util.List;
public class UserRepository extends Application {
    private List<User> listaUtilizadores = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

    }
    public void adicionar(User user) {
        listaUtilizadores.add(user);
    }
    public User encontrarPorCredenciais(String nome, String password) {
        for (User util : listaUtilizadores) {
            if (util.getNome().equals(nome) && util.getPassword().equals(password)) {
                return util;
            }
        }
        return null;
    }
}