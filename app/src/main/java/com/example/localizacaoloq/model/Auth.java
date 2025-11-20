package com.example.localizacaoloq.model;

import com.example.localizacaoloq.Repository.UserRepository;

public class Auth {
   private User user;
   private String sessionId;
   private  boolean ative=true;

    public boolean isAtive() {
        return ative;
    }

    public void setAtive(boolean ative) {
        this.ative = ative;
    }

    public  Auth(User utilizador, String idSesssion){
       this.sessionId=idSesssion;
       this.user=utilizador;
   }

    public String getIdSession() {
        return sessionId;
    }


    public void setIdSession(String idSession) {
        this.sessionId = idSession;
    }

    public User getUtilizador() {
        return user;
    }

    public void setUtilizador(User utilizador) {
        this.user = utilizador;
    }
}
