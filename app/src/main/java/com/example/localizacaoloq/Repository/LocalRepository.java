package com.example.localizacaoloq.Repository;

import android.util.Log;

import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.LocalGPS;
import com.example.localizacaoloq.model.LocalWifi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class LocalRepository extends ApiReposistory {
    private static LocalRepository instance;
    private final List<Local> locais = new ArrayList<>();
    private boolean cacheCarregado = false;

    private LocalRepository() {}

    public static LocalRepository getInstance() {
        if (instance == null) {
            instance = new LocalRepository();
        }
        return instance;
    }
    public List<Local> getLocais() {
        return new ArrayList<>(locais);
    }
    public void setLocais(List<Local> lista) {
        locais.clear();
        locais.addAll(lista);
        cacheCarregado = true;
    }

    public boolean isCacheCarregado() {
        return cacheCarregado;
    }

    public void limparCache() {
        locais.clear();
        cacheCarregado = false;
    }

    private void addLocal(Local local) {
        if (local != null) {
            locais.add(local);
        }
    }

    private void removeLocal(String id) {
        locais.removeIf(l -> l.get_id().equals(id));
    }

    private void updateLocal(Local localAtualizado) {
        if (localAtualizado == null) return;

        for (int i = 0; i < locais.size(); i++) {
            if (locais.get(i).get_id().equals(localAtualizado.get_id())) {
                locais.set(i, localAtualizado);
                break;
            }
        }
    }

    public Local create(Local local) {
        try {
            URL url = new URL(baseUrl + "/locais");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("nome", local.getNome());

            if (local instanceof LocalGPS) {
                json.put("tipo", "GPS");
                LocalGPS gps = (LocalGPS) local;
                json.put("latitude", gps.getLatitude());
                json.put("longitude", gps.getLongitude());
                json.put("raio", gps.getRaio());
            } else if (local instanceof LocalWifi) {
                json.put("tipo", "WIFI");
                LocalWifi wifi = (LocalWifi) local;
                JSONArray sinais = new JSONArray(wifi.getSinal());
                json.put("sinal", sinais);
            }

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

            if (code >= 200 && code < 300) {
                JSONObject resJson = new JSONObject(response.toString());
                Local novoLocal = parseLocal(resJson);

                if (novoLocal != null) {
                    addLocal(novoLocal);
                }

                return novoLocal;
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Local> findAll() {
        return findAll(false);
    }

    public List<Local> findAll(boolean forcarAtualizacao) {
        if (cacheCarregado && !forcarAtualizacao) {
            return new ArrayList<>(locais);
        }

        try {
            URL url = new URL(baseUrl + "/locais");
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
                List<Local> lista = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Log.d("API_JSON_OBJ", obj.toString());
                    lista.add(parseLocal(obj));
                }

                setLocais(lista);

                return lista;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Local findById(String id) {
        return findById(id, false);
    }

    public Local findById(String id, boolean forcarAtualizacao) {
        if (!forcarAtualizacao && cacheCarregado) {
            for (Local local : locais) {
                if (local.get_id().equals(id)) {
                    return local;
                }
            }
        }

        try {
            URL url = new URL(baseUrl + "/locais/" + id);
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
                JSONObject obj = new JSONObject(response.toString());
                Local local = parseLocal(obj);

                // ✅ Atualiza/adiciona no cache se não existir
                if (local != null && cacheCarregado) {
                    boolean existe = false;
                    for (int i = 0; i < locais.size(); i++) {
                        if (locais.get(i).get_id().equals(id)) {
                            locais.set(i, local);
                            existe = true;
                            break;
                        }
                    }
                    if (!existe) {
                        addLocal(local);
                    }
                }

                return local;
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Local> searchByName(String query) {
        try {
            String queryParam = "?nome=" + query;

            URL url = new URL(baseUrl + "/locais/search/by-name" + queryParam);
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
                List<Local> lista = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    lista.add(parseLocal(arr.getJSONObject(i)));
                }
                return lista;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public boolean delete(String id) {
        try {
            URL url = new URL(baseUrl + "/locais/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            conn.disconnect();

            if (code >= 200 && code < 300) {
                // ✅ Remove do cache local
                removeLocal(id);
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Local parseLocal(JSONObject obj) {
        try {
            String id = obj.getString("_id");
            String tipo = obj.optString("tipo", "");
            String nome = obj.optString("nome", "Desconhecido");

            if ("GPS".equalsIgnoreCase(tipo)) {
                double lat = obj.optDouble("latitude", 0);
                double lon = obj.optDouble("longitude", 0);
                double raio = obj.optDouble("raio", 0);
                LocalGPS lgps = new LocalGPS(nome, lat, lon, raio);
                lgps.set_id(id);
                return lgps;
            } else if ("WIFI".equalsIgnoreCase(tipo)) {
                List<String> sinais = new ArrayList<>();
                JSONArray arr = obj.optJSONArray("sinal");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) sinais.add(arr.getString(i));
                }
                LocalWifi lsinal = new LocalWifi(nome, sinais);
                lsinal.set_id(id);
                return lsinal;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}