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
    private boolean cacheCarregado = false;

    private PerfilReposistory() {}

    public static PerfilReposistory getInstance() {
        if (instance == null) {
            instance = new PerfilReposistory();
        }
        return instance;
    }

    public List<Perfil> getPerfis() {
        return new ArrayList<>(perfis);
    }
    public void setPerfis(List<Perfil> lista) {
        perfis.clear();
        perfis.addAll(lista);
        cacheCarregado = true;
    }

    public boolean isCacheCarregado() {
        return cacheCarregado;
    }

    public void limparCache() {
        perfis.clear();
        cacheCarregado = false;
    }

    public Perfil addPerfil(String idUsuario, Perfil perfil) {
        try {
            URL url = new URL(baseUrl + "/users/" + idUsuario + "/perfil");
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

            if (code >= 200 && code < 300) {
                Perfil novoPerfil = parsePerfil(new JSONObject(response.toString()));
                if (novoPerfil != null) {
                    perfis.add(novoPerfil);
                }

                return novoPerfil;
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean removePerfil(String idUsuario, String idPerfil) {
        try {
            URL url = new URL(baseUrl + "/users/" + idUsuario + "/perfil/" + idPerfil);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            conn.disconnect();

            if (code >= 200 && code < 300) {
                perfis.removeIf(p -> p.get_id().equals(idPerfil));
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Perfil editarPerfil(String idUsuario, String idPerfil, Perfil perfil) {
        try {
            URL url = new URL(baseUrl + "/users/" + idUsuario + "/perfil/" + idPerfil);
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

            if (code >= 200 && code < 300) {
                Perfil perfilAtualizado = parsePerfil(new JSONObject(response.toString()));
                if (perfilAtualizado != null) {
                    for (int i = 0; i < perfis.size(); i++) {
                        if (perfis.get(i).get_id().equals(idPerfil)) {
                            perfis.set(i, perfilAtualizado);
                            break;
                        }
                    }
                }

                return perfilAtualizado;
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Perfil> listarPerfisUsuario(String idUsuario) {
        return listarPerfisUsuario(idUsuario, false);
    }
    public List<Perfil> listarPerfisUsuario(String idUsuario, boolean forcarAtualizacao) {
        if (cacheCarregado && !forcarAtualizacao) {
            return new ArrayList<>(perfis);
        }
        try {
            URL url = new URL(baseUrl + "/users/perfil-all/" + idUsuario);
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
                List<Perfil> lista = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    lista.add(parsePerfil(arr.getJSONObject(i)));
                }
                setPerfis(lista);

                return lista;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
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