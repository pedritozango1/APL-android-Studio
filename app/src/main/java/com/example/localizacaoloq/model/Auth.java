package com.example.localizacaoloq.model;

import com.example.localizacaoloq.Repository.UserRepository;

public class Auth {
   private User user;
   private String idSession;
   private  boolean ative=true;

    public boolean isAtive() {
        return ative;
    }

    public void setAtive(boolean ative) {
        this.ative = ative;
    }

    public  Auth(User utilizador, String idSesssion){
       this.idSession=idSesssion;
       this.user=utilizador;
   }

    public String getIdSession() {
        return idSession;
    }


    public void setIdSession(String idSession) {
        this.idSession = idSession;
    }

    public User getUtilizador() {
        return user;
    }

    public void setUtilizador(User utilizador) {
        this.user = utilizador;
    }
}
