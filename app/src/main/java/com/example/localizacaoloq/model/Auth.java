package com.example.localizacaoloq.model;

import java.util.ArrayList;

public class Auth {
    private UserRepository repository;
    private SessionManager sessionManager;

    public Auth(UserRepository repository, SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    // Método de login (recebe credenciais diretamente, retorna ID de sessão)
    public String login(String nome, String password) {
        User user = repository.encontrarPorCredenciais(nome, password);
        if (user != null) {
            String sessionId = sessionManager.criarSessao(user);
            System.out.println("Login bem-sucedido! Sessão: " + sessionId);
            return sessionId;
        }
        System.out.println("Credenciais inválidas!");
        return null;
    }

    // Método para verificar se uma sessão é válida
    public boolean sessaoValida(String sessionId) {
        return sessionManager.sessaoValida(sessionId);
    }

    // Método de logout
    public void logout(String sessionId) {
        sessionManager.terminarSessao(sessionId);
        System.out.println("Sessão terminada!");
    }
}
