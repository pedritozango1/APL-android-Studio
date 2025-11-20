package com.example.localizacaoloq.Repository;

import android.content.Context;
import android.util.Log;

import com.example.localizacaoloq.model.Anuncio;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.LocalGPS;
import com.example.localizacaoloq.model.LocalWifi;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnuncioRepository extends ApiReposistory {
    private static AnuncioRepository instance;
    private final List<Anuncio> anuncios = new ArrayList<>();
    private Context context;

    private AnuncioRepository() {}

    public static AnuncioRepository getInstance() {
        if (instance == null) {
            instance = new AnuncioRepository();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<Anuncio> getAnuncios() {
        return anuncios;
    }

    public void addAnuncio(Anuncio anuncio) {
        anuncios.add(anuncio);
    }

    public void removeAnuncio(Anuncio anuncio) {
        anuncios.remove(anuncio);
    }

    public void setAnuncios(List<Anuncio> lista) {
        anuncios.clear();
        anuncios.addAll(lista);
    }

    public Anuncio create(Anuncio anuncio) {
        try {
            URL url = new URL(baseUrl + "/anuncios");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            JSONObject json = new JSONObject();
            json.put("titulo", anuncio.getTitulo());
            json.put("mensagem", anuncio.getMensagem());

            // Verificar se local não é nulo
            if (anuncio.getLocal() != null && anuncio.getLocal().get_id() != null) {
                json.put("local", anuncio.getLocal().get_id());
            } else {
                Log.e("AnuncioRepository", "Local é nulo ou sem ID");
                return null;
            }

            // Enviar username do usuário
            if (anuncio.getUser() != null && anuncio.getUser().getUsername() != null) {
                json.put("user", anuncio.getUser().getUsername());
            } else {
                Log.e("AnuncioRepository", "User é nulo ou sem username");
                return null;
            }

            json.put("modoEntrega", anuncio.getModoEntrega());
            json.put("politica", anuncio.getPolitica());
            json.put("inicio", dateFormat.format(anuncio.getInicio()));
            json.put("fim", dateFormat.format(anuncio.getFim()));

            Log.d("AnuncioRepository", "JSON enviado: " + json.toString());

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(json.toString());
            writer.flush();
            writer.close();

            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);

            reader.close();
            conn.disconnect();

            Log.d("AnuncioRepository", "Resposta da API: " + response.toString() + " Código: " + code);

            if (code >= 200 && code < 300) {
                JSONObject resJson = new JSONObject(response.toString());
                return parseAnuncio(resJson);
            } else {
                Log.e("AnuncioRepository", "Erro ao criar anúncio: " + response.toString());
                return null;
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no create: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Anuncio> findAll() {
        try {
            URL url = new URL(baseUrl + "/anuncios");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);

            reader.close();
            conn.disconnect();

            if (code >= 200 && code < 300) {
                JSONArray arr = new JSONArray(response.toString());
                List<Anuncio> anunciosList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Log.d("AnuncioRepository", "JSON Object: " + obj.toString());
                    Anuncio anuncio = parseAnuncio(obj);
                    if (anuncio != null) {
                        anunciosList.add(anuncio);
                    }
                }
                return anunciosList;
            } else {
                Log.e("AnuncioRepository", "Erro ao buscar anúncios: " + response.toString());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no findAll: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Anuncio parseAnuncio(JSONObject obj) {
        try {
            Anuncio anuncio = new Anuncio();

            // Campos básicos
            anuncio.set_id(obj.getString("_id"));
            anuncio.setTitulo(obj.getString("titulo"));
            anuncio.setMensagem(obj.getString("mensagem"));
            anuncio.setModoEntrega(obj.getString("modoEntrega"));
            anuncio.setPolitica(obj.getString("politica"));

            // Processar local com suporte a GPS e WIFI (sempre populado)
            if (obj.has("local") && !obj.isNull("local")) {
                JSONObject localJson = obj.getJSONObject("local");
                String tipo = localJson.optString("tipo", "");
                String id = localJson.getString("_id");
                String nome = localJson.optString("nome", "Desconhecido");

                Local local;
                if ("GPS".equalsIgnoreCase(tipo)) {
                    double lat = localJson.optDouble("latitude", 0);
                    double lon = localJson.optDouble("longitude", 0);
                    double raio = localJson.optDouble("raio", 0);
                    LocalGPS gps = new LocalGPS(nome, lat, lon, raio);
                    gps.set_id(id);
                    local = gps;
                    anuncio.setLocal(local);
                } else if ("WIFI".equalsIgnoreCase(tipo)) {
                    List<String> sinais = new ArrayList<>();
                    JSONArray arr = localJson.optJSONArray("sinal");
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            sinais.add(arr.getString(i));
                        }
                    }
                    LocalWifi wifi = new LocalWifi(nome, sinais);
                    wifi.set_id(id);
                    local = wifi;
                    anuncio.setLocal(local);
                }

            }

            // Processar user (sempre populado) - user é STRING username
            if (obj.has("user") && !obj.isNull("user")) {
                Object userObj = obj.get("user");

                if (userObj instanceof String) {
                    String username = (String) userObj;
                    User user = new User(username, "");
                    anuncio.setUser(user);
                } else if (userObj instanceof JSONObject) {
                    JSONObject userJson = (JSONObject) userObj;
                    String username = userJson.optString("username", "");
                    String password = userJson.optString("password", "");
                    User user = new User(username, password);
                    anuncio.setUser(user);
                }
            }

            // Processamento das datas
            String inicioStr = obj.getString("inicio");
            String fimStr = obj.getString("fim");

            inicioStr = inicioStr.replace("Z", "").split("\\.")[0];
            fimStr = fimStr.replace("Z", "").split("\\.")[0];

            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            java.util.Date inicio = simpleFormat.parse(inicioStr);
            java.util.Date fim = simpleFormat.parse(fimStr);

            anuncio.setInicio(inicio);
            anuncio.setFim(fim);

            // Campos de timestamp
            if (obj.has("createdAt")) {
                String createdAtStr = obj.getString("createdAt");
                createdAtStr = createdAtStr.replace("Z", "").split("\\.")[0];
                java.util.Date createdAt = simpleFormat.parse(createdAtStr);
                anuncio.setCreatedAt(createdAt);
            }

            if (obj.has("updatedAt")) {
                String updatedAtStr = obj.getString("updatedAt");
                updatedAtStr = updatedAtStr.replace("Z", "").split("\\.")[0];
                java.util.Date updatedAt = simpleFormat.parse(updatedAtStr);
                anuncio.setUpdatedAt(updatedAt);
            }

            return anuncio;

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no parseAnuncio: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Método para obter o usuário logado via Session
    public User getCurrentUser() {
        try {
            if (context == null) {
                Log.e("AnuncioRepository", "Context não foi definido");
                return null;
            }

            SessionManager sessionManager = new SessionManager(context);
            String sessionId = sessionManager.getSessionId();

            if (sessionId != null && !sessionId.isEmpty()) {
                AuthRepository authRepo = new AuthRepository();
                Session session = authRepo.pegarIdSessao(sessionId);

                if (session != null && session.isActive()) {
                    User user = session.getUser();

                    if (user != null) {
                        Log.d("AnuncioRepository", "Usuário obtido da sessão: " + user.getUsername());
                        return user;
                    } else {
                        Log.e("AnuncioRepository", "Username vazio na sessão");
                    }
                } else {
                    Log.e("AnuncioRepository", "Sessão inválida ou inativa");
                }
            } else {
                Log.e("AnuncioRepository", "SessionId não encontrado");
            }
        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro ao obter usuário: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}