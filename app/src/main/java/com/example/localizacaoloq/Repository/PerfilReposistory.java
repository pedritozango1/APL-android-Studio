package com.example.localizacaoloq.Repository;
import com.example.localizacaoloq.model.Perfil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PerfilReposistory extends ApiReposistory {

    private static PerfilReposistory instance;
    private final List<Perfil> perfis = new ArrayList<>();

    private PerfilReposistory() {}

    public static PerfilReposistory getInstance() {
        if (instance == null) {
            instance = new PerfilReposistory();
        }
        return instance;
    }

    public List<Perfil> getPerfis() {
        return perfis;
    }

    public void addPerfil(Perfil p) {
        perfis.add(p);
    }

    public void removePerfil(Perfil p) {
        perfis.remove(p);
    }

    public void setPerfis(List<Perfil> lista) {
        perfis.clear();
        perfis.addAll(lista);
    }

    // ------------------- CREATE -------------------
    public Perfil create(Perfil perfil) {
        try {
            URL url = new URL(baseUrl + "/perfis");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("valor", perfil.getValor());
            json.put("chave", perfil.getChave());

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

            return parsePerfil(new JSONObject(response.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------- FIND ALL -------------------
    public List<Perfil> findAll() {
        try {
            URL url = new URL(baseUrl + "/perfis");
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

            JSONArray arr = new JSONArray(response.toString());
            List<Perfil> lista = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++) {
                lista.add(parsePerfil(arr.getJSONObject(i)));
            }

            return lista;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------- FIND BY ID -------------------
    public Perfil findById(String id) {
        try {
            URL url = new URL(baseUrl + "/perfis/" + id);
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

            return parsePerfil(new JSONObject(response.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------- UPDATE -------------------
    public Perfil update(String id, Perfil perfil) {
        try {
            URL url = new URL(baseUrl + "/perfis/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("valor", perfil.getValor());
            json.put("chave", perfil.getChave());

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

            return parsePerfil(new JSONObject(response.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------- DELETE -------------------
    public boolean delete(String id) {
        try {
            URL url = new URL(baseUrl + "/perfis/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();

            return code >= 200 && code < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- SEARCH BY VALOR -------------------
    public List<Perfil> searchByValor(String valor) {
        try {
            URL url = new URL(baseUrl + "/perfis/search/by-valor?q=" + valor);
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

            JSONArray arr = new JSONArray(response.toString());
            List<Perfil> lista = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++) {
                lista.add(parsePerfil(arr.getJSONObject(i)));
            }

            return lista;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ------------------- PARSER -------------------
    private Perfil parsePerfil(JSONObject obj) {
        try {
            Perfil p = new Perfil();
            p.set_id(obj.getString("_id"));
            p.setValor(obj.optString("valor", ""));
            p.setChave(obj.optString("chave", ""));
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

