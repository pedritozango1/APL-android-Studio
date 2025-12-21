package com.example.localizacaoloq.Repository;


import android.util.Log;

import com.example.localizacaoloq.model.Chave;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChaveRepository extends ApiReposistory {
    private static ChaveRepository instance;
    private final List<Chave> chaves = new ArrayList<>();
    private boolean cacheCarregado = false;

    private ChaveRepository() {}

    public static ChaveRepository getInstance() {
        if (instance == null) {
            instance = new ChaveRepository();
        }
        return instance;
    }

    public List<Chave> getChaves() {
        return new ArrayList<>(chaves);
    }

    public void setChaves(List<Chave> lista) {
        chaves.clear();
        chaves.addAll(lista);
        cacheCarregado = true;
    }

    public boolean isCacheCarregado() {
        return cacheCarregado;
    }

    public void limparCache() {
        chaves.clear();
        cacheCarregado = false;
    }

    private void addChave(Chave chave) {
        if (chave != null) {
            chaves.add(chave);
        }
    }

    public List<Chave> findAll() {
        return findAll(false);
    }

    public List<Chave> findAll(boolean forcarAtualizacao) {
        if (cacheCarregado && !forcarAtualizacao) {
            return new ArrayList<>(chaves);
        }
        try {
            URL url = new URL(baseUrl + "/chaves");
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
                List<Chave> lista = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Log.d("API_JSON_OBJ", obj.toString());
                    Chave chave = parseChave(obj);
                    if (chave != null) {
                        lista.add(chave);
                    }
                }
                setChaves(lista);
                return lista;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Chave parseChave(JSONObject obj) {
        try {
            String id = obj.getString("_id");
            String nome = obj.optString("nome", "Desconhecido");
            Chave chave = new Chave(nome);
            chave.set_id(id);
            return chave;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
