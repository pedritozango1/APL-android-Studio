package com.example.localizacaoloq.Repository;

import android.util.Log;

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

                JSONObject userJson = jsonResponse.getJSONObject("user");

                User userObj = new User(
                        userJson.getString("username"),
                        userJson.optString("password") // opcional caso não venha
                );
                userObj.set_id(userJson.getString("_id"));

                String sessionId = jsonResponse.getString("sessionId");
                boolean active = jsonResponse.getBoolean("active");

                Session session = new Session();
                session.setUser(userObj);
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
    private static final String TAG = "SessionAPI";

    public Session pegarIdSessao(String idSessao) {
        try {
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
                // DEBUG: Resposta completa
                Log.d(TAG, "Resposta completa: " + response.toString());

                JSONObject jsonResponse = new JSONObject(response.toString());
                User userObj = null;

                // DEBUG: Verificar campo "user"
                if (jsonResponse.has("user")) {
                    Log.d(TAG, "Campo 'user' encontrado");
                    Object userField = jsonResponse.get("user");
                    Log.d(TAG, "Tipo do campo 'user': " + userField.getClass().getName());
                    Log.d(TAG, "Conteúdo do campo 'user': " + userField.toString());

                    // Verificar se não é null e se é JSONObject
                    if (userField != null && userField != JSONObject.NULL && userField instanceof JSONObject) {
                        JSONObject userJson = (JSONObject) userField;

                        Log.d(TAG, "Username: " + userJson.optString("username", "N/A"));
                        Log.d(TAG, "User ID: " + userJson.optString("_id", "N/A"));

                        userObj = new User(
                                userJson.optString("username", ""),
                                userJson.optString("password", "")
                        );
                        userObj.set_id(userJson.optString("_id", ""));

                        Log.d(TAG, "User criado com sucesso: " + userObj.get_id());
                    } else {
                        Log.w(TAG, "Campo 'user' é null ou não é JSONObject!");
                    }
                } else {
                    Log.w(TAG, "Campo 'user' NÃO existe no JSON!");
                }

                String sessionId = jsonResponse.optString("sessionId", "");
                boolean active = jsonResponse.optBoolean("active", false);

                Log.d(TAG, "SessionId: " + sessionId + ", Active: " + active);

                Session session = new Session();
                session.setUser(userObj);
                session.setSessionId(sessionId);
                session.setActive(active);

                return session;

            } else {
                Log.e(TAG, "Erro HTTP: " + responseCode + " - " + response.toString());
                throw new Exception("Erro (" + responseCode + "): " + response.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "Exceção ao pegar sessão: " + e.getMessage(), e);
            return null;
        }
    }
}

