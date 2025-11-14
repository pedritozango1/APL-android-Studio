package com.example.localizacaoloq.Repository;

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
    public Local create(Local local) {
        try {
            URL url = new URL(baseUrl + "/locais");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("nome", local.getNome());

            if (local instanceof LocalGPS ) {
                LocalGPS gps = (LocalGPS) local;
                json.put("latitude", gps.getLatitude());
                json.put("longitude", gps.getLongitude());
                json.put("altitude", gps.getAltitude());
                json.put("raio", gps.getRaio());
            } else if (local instanceof LocalWifi) {
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

            JSONObject resJson = new JSONObject(response.toString());
            // Retornar objeto Local correto
            return parseLocal(resJson);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Local> findAll() {
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

            JSONArray arr = new JSONArray(response.toString());
            List<Local> locais = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                locais.add(parseLocal(obj));
            }
            return locais;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Local findById(String id) {
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

            JSONObject obj = new JSONObject(response.toString());
            return parseLocal(obj);

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

            JSONArray arr = new JSONArray(response.toString());
            List<Local> locais = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                locais.add(parseLocal(arr.getJSONObject(i)));
            }
            return locais;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private Local parseLocal(JSONObject obj) {
        try {
            String tipo = obj.optString("tipo", "");
            String nome = obj.optString("nome", "Desconhecido");

            if ("GPS".equalsIgnoreCase(tipo)) {
                double lat = obj.optDouble("latitude", 0);
                double lon = obj.optDouble("longitude", 0);
                double alt = obj.optDouble("altitude", 0);
                double raio = obj.optDouble("raio", 0);
                return new LocalGPS(nome, lat, lon, alt, raio);
            } else if ("WIFI".equalsIgnoreCase(tipo)) {
                List<String> sinais = new ArrayList<>();
                JSONArray arr = obj.optJSONArray("sinal");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) sinais.add(arr.getString(i));
                }
                return new LocalWifi(nome, sinais);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
