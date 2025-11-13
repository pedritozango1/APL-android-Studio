package com.example.localizacaoloq.Repository;

import com.example.localizacaoloq.model.Auth;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.User;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
public class AuthRepository extends ApiReposistory {
    public Session login(User user) {
        try {
            URL url = new URL(baseUrl + "/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Corpo JSON da requisição
            JSONObject json = new JSONObject();
            json.put("username", user.getUsername());
            json.put("password", user.getPassword());

            // Envio do corpo
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(json.toString());
            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();
            BufferedReader reader;

            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            if (responseCode >= 200 && responseCode < 300) {
                 JSONObject jsonResponse = new JSONObject(response.toString());

                String username = jsonResponse.getJSONObject("user").getString("username"); // ou id se quiser
                String sessionId = jsonResponse.getString("sessionId");
                boolean active = jsonResponse.getBoolean("active");

                Session session = new Session();
                session.setUser(username);
                session.setSessionId(sessionId);
                session.setActive(active);

                return session;
            } else {
                throw new Exception("Erro (" + responseCode + "): " + response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String logout(String sessionId) {
        try {
            URL url = new URL(baseUrl + "/auth/logout");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Corpo JSON
            JSONObject json = new JSONObject();
            json.put("sessionId", sessionId);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(json.toString());
            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();
            BufferedReader reader;

            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            return response.toString();

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
    public Session pegarIdSessao(String idSessao) {
        try {
            // URL do endpoint NestJS
            URL url = new URL(baseUrl + "/auth/pegarSessao/" + idSessao);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            BufferedReader reader;

            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            if (responseCode >= 200 && responseCode < 300) {
                // A API retorna um objeto JSON do tipo Session
                JSONObject jsonResponse = new JSONObject(response.toString());

                // Se o campo "user" for um objeto, podemos extrair o ID ou nome
                String user = "";
                if (jsonResponse.has("user")) {
                    Object userField = jsonResponse.get("user");
                    if (userField instanceof JSONObject) {
                        user = ((JSONObject) userField).getString("username");
                    } else {
                        user = jsonResponse.getString("user");
                    }
                }

                String sessionId = jsonResponse.optString("sessionId", "");
                boolean active = jsonResponse.optBoolean("active", false);

                Session session = new Session();
                session.setUser(user);
                session.setSessionId(sessionId);
                session.setActive(active);

                return session;
            } else {
                throw new Exception("Erro (" + responseCode + "): " + response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

