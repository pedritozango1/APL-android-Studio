package com.example.localizacaoloq.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private Map<String, User> sessoesAtivas = new HashMap<>();

    // Método para criar uma sessão e retornar o ID
    public String criarSessao(User user) {
        String sessionId = UUID.randomUUID().toString();
        sessoesAtivas.put(sessionId, user);
        return sessionId;
    }

    // Método para verificar se uma sessão é válida
    public boolean sessaoValida(String sessionId) {
        return sessoesAtivas.containsKey(sessionId);
    }

    // Método para terminar (logout) uma sessão
    public void terminarSessao(String sessionId) {
        sessoesAtivas.remove(sessionId);
    }
}
