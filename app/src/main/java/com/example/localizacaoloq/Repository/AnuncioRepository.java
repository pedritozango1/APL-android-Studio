package com.example.localizacaoloq.Repository;

import android.content.Context;
import android.util.Log;

import com.example.localizacaoloq.model.Anuncio;
import com.example.localizacaoloq.model.Chave;
import com.example.localizacaoloq.model.ListaChave;
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

    /**
     * Criar novo anúncio
     * INSERÇÃO: Envia apenas IDs (strings)
     */
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

            // Local: Enviar apenas ID (string)
            if (anuncio.getLocal() != null && anuncio.getLocal().get_id() != null) {
                json.put("local", anuncio.getLocal().get_id());
            } else {
                Log.e("AnuncioRepository", "Local é nulo ou sem ID");
                return null;
            }

            // ListaChave: Enviar array de { chaveId: string, valor: string }
            if (anuncio.getListaChave() != null && !anuncio.getListaChave().isEmpty()) {
                JSONArray listaChaveArray = new JSONArray();
                for (ListaChave restricao : anuncio.getListaChave()) {
                    JSONObject restricaoJson = new JSONObject();
                    // Pegar ID da chave do objeto Chave
                    restricaoJson.put("chaveId", restricao.getChave().get_id());
                    restricaoJson.put("valor", restricao.getValor());
                    listaChaveArray.put(restricaoJson);
                }
                json.put("listaChave", listaChaveArray);
            }

            // User: Enviar apenas ID (string)
            if (anuncio.getUser() != null) {
                if (anuncio.getUser().get_id() != null && !anuncio.getUser().get_id().isEmpty()) {
                    json.put("user", anuncio.getUser().get_id());
                } else if (anuncio.getUser().getUsername() != null) {
                    json.put("user", anuncio.getUser().getUsername());
                } else {
                    Log.e("AnuncioRepository", "User sem ID ou username");
                    return null;
                }
            } else {
                Log.e("AnuncioRepository", "User é nulo");
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

    /**
     * Buscar todos os anúncios
     * RETORNO: Vem com populate (objetos completos)
     */
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
     //Buscar por id do utilizador
     public List<Anuncio> findAnuncioUtilizador(String idUsuario) {
         try {
             URL url = new URL(baseUrl + "/anuncios/findUsuarios/"+idUsuario);
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
    /**
     * Buscar anúncios ativos
     */
    public List<Anuncio> findAtivos() {
        try {
            URL url = new URL(baseUrl + "/anuncios/ativos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            return executeListRequest(conn);

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no findAtivos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Buscar anúncio por ID
     */
    public Anuncio findById(String id) {
        try {
            URL url = new URL(baseUrl + "/anuncios/" + id);
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
                return parseAnuncio(obj);
            } else {
                Log.e("AnuncioRepository", "Erro ao buscar anúncio: " + response.toString());
                return null;
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no findById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletar anúncio
     */
    public boolean delete(String id) {
        try {
            URL url = new URL(baseUrl + "/anuncios/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            int code = conn.getResponseCode();
            conn.disconnect();

            if (code >= 200 && code < 300) {
                Log.d("AnuncioRepository", "Anúncio deletado com sucesso");
                return true;
            } else {
                Log.e("AnuncioRepository", "Erro ao deletar anúncio. Código: " + code);
                return false;
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no delete: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // No AnuncioRepository.java, adicione este método:

    public List<Anuncio> getAnunciosProximosPorLocalizacao(double latitude, double longitude, String username, List<String> ssids) {
        try {
            URL url = new URL(baseUrl + "/Repostarlocalizacao/reportar");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Criar JSON com localização
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("latitude", latitude);
            json.put("longitude", longitude);

            if (ssids != null && !ssids.isEmpty()) {
                JSONArray ssidsArray = new JSONArray();
                for (String ssid : ssids) {
                    ssidsArray.put(ssid);
                }
                json.put("ssids", ssidsArray);
            }

            Log.d("AnuncioRepository", "Enviando localização: " + json.toString());

            // Enviar requisição
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

            Log.d("AnuncioRepository", "Resposta anúncios próximos: " + response.toString() + " Código: " + code);

            if (code >= 200 && code < 300) {
                JSONObject respostaJson = new JSONObject(response.toString());
                JSONArray anunciosArray = respostaJson.getJSONArray("anuncios");

                List<Anuncio> anunciosProximos = new ArrayList<>();

                for (int i = 0; i < anunciosArray.length(); i++) {
                    JSONObject anuncioJson = anunciosArray.getJSONObject(i);

                    // Converter para objeto Anuncio
                    Anuncio anuncio = new Anuncio();
                    anuncio.set_id(anuncioJson.getString("id"));
                    anuncio.setTitulo(anuncioJson.getString("titulo"));
                    anuncio.setMensagem(anuncioJson.getString("mensagem"));
                    anuncio.setModoEntrega(anuncioJson.getString("modoEntrega"));
                    anuncio.setPolitica(anuncioJson.getString("politica"));

                    // Local
                    if (anuncioJson.has("local") && !anuncioJson.isNull("local")) {
                        JSONObject localJson = anuncioJson.getJSONObject("local");
                        String tipo = localJson.optString("tipo", "");
                        String nome = localJson.optString("nome", "Desconhecido");

                        if ("GPS".equalsIgnoreCase(tipo)) {
                            LocalGPS local = new LocalGPS(nome, 0, 0, 0);
                            anuncio.setLocal(local);
                        } else if ("WIFI".equalsIgnoreCase(tipo)) {
                            LocalWifi local = new LocalWifi(nome, new ArrayList<>());
                            anuncio.setLocal(local);
                        }
                    }

                    // Datas
                    if (anuncioJson.has("inicio")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        anuncio.setInicio(sdf.parse(anuncioJson.getString("inicio")));
                    }

                    if (anuncioJson.has("fim")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        anuncio.setFim(sdf.parse(anuncioJson.getString("fim")));
                    }

                    anunciosProximos.add(anuncio);
                }

                return anunciosProximos;
            } else {
                Log.e("AnuncioRepository", "Erro ao buscar anúncios próximos: " + response.toString());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no getAnunciosProximosPorLocalizacao: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public List<Anuncio> getAnunciosProximos(double latitude, double longitude, String username) {
        try {
            URL url = new URL(baseUrl + "/anuncios/proximos/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("latitude", latitude);
            json.put("longitude", longitude);

            Log.d("AnuncioRepository", "JSON enviado para próximos: " + json.toString());

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

            Log.d("AnuncioRepository", "Resposta da API próximos: " + response.toString() + " Código: " + code);

            if (code >= 200 && code < 300) {
                JSONArray arr = new JSONArray(response.toString());
                List<Anuncio> anunciosList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Anuncio anuncio = parseAnuncio(obj);
                    if (anuncio != null) {
                        anunciosList.add(anuncio);
                    }
                }
                return anunciosList;
            } else {
                Log.e("AnuncioRepository", "Erro ao buscar anúncios próximos: " + response.toString());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no getAnunciosProximos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    /**
     * Parse do JSON para objeto Anuncio
     * RETORNO: Vem com populate (objetos completos)
     */
    private Anuncio parseAnuncio(JSONObject obj) {
        try {
            Anuncio anuncio = new Anuncio();

            // Campos básicos
            anuncio.set_id(obj.getString("_id"));
            anuncio.setTitulo(obj.getString("titulo"));
            anuncio.setMensagem(obj.getString("mensagem"));
            anuncio.setModoEntrega(obj.getString("modoEntrega"));
            anuncio.setPolitica(obj.getString("politica"));

            // Local (vem populado com GPS ou WIFI)
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

            // ListaChave (vem populada com objetos Chave completos)
            if (obj.has("listaChave") && !obj.isNull("listaChave")) {
                JSONArray listaChaveArray = obj.getJSONArray("listaChave");
                List<ListaChave> listaChave = new ArrayList<>();

                for (int i = 0; i < listaChaveArray.length(); i++) {
                    JSONObject restricaoJson = listaChaveArray.getJSONObject(i);

                    String valor = restricaoJson.getString("valor");
                    Chave chave = null;

                    // chaveId vem populado como objeto
                    if (restricaoJson.has("chaveId") && !restricaoJson.isNull("chaveId")) {
                        Object chaveIdObj = restricaoJson.get("chaveId");

                        if (chaveIdObj instanceof JSONObject) {
                            // Chave populada (objeto completo)
                            JSONObject chaveJson = (JSONObject) chaveIdObj;
                            chave = new Chave();
                            chave.set_id(chaveJson.getString("_id"));
                            chave.setNome(chaveJson.getString("nome"));
                        } else if (chaveIdObj instanceof String) {
                            // Chave não populada (apenas ID - não deveria acontecer com populate)
                            chave = new Chave();
                            chave.set_id((String) chaveIdObj);
                        }
                    }

                    if (chave != null) {
                        ListaChave restricao = new ListaChave(chave, valor);
                        listaChave.add(restricao);
                    }
                }

                anuncio.setListaChave(listaChave);
            }

            // User (vem populado com objeto completo)
            if (obj.has("user") && !obj.isNull("user")) {
                Object userObj = obj.get("user");

                if (userObj instanceof String) {
                    // User não populado (apenas ID - não deveria acontecer)
                    User user = new User();
                    user.set_id((String) userObj);
                    anuncio.setUser(user);
                } else if (userObj instanceof JSONObject) {
                    // User populado (objeto completo)
                    JSONObject userJson = (JSONObject) userObj;
                    User user = new User();
                    user.set_id(userJson.optString("_id", ""));
                    user.setUsername(userJson.optString("username", ""));
                    anuncio.setUser(user);
                }
            }

            // Datas
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            String inicioStr = obj.getString("inicio").replace("Z", "").split("\\.")[0];
            String fimStr = obj.getString("fim").replace("Z", "").split("\\.")[0];

            anuncio.setInicio(simpleFormat.parse(inicioStr));
            anuncio.setFim(simpleFormat.parse(fimStr));

            // Timestamps
            if (obj.has("createdAt")) {
                String createdAtStr = obj.getString("createdAt").replace("Z", "").split("\\.")[0];
                anuncio.setCreatedAt(simpleFormat.parse(createdAtStr));
            }

            if (obj.has("updatedAt")) {
                String updatedAtStr = obj.getString("updatedAt").replace("Z", "").split("\\.")[0];
                anuncio.setUpdatedAt(simpleFormat.parse(updatedAtStr));
            }

            return anuncio;

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no parseAnuncio: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método auxiliar para executar requisições que retornam lista
     */
    private List<Anuncio> executeListRequest(HttpURLConnection conn) {
        try {
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
                    Anuncio anuncio = parseAnuncio(obj);
                    if (anuncio != null) {
                        anunciosList.add(anuncio);
                    }
                }
                return anunciosList;
            } else {
                Log.e("AnuncioRepository", "Erro na requisição: " + response.toString());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            Log.e("AnuncioRepository", "Erro no executeListRequest: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obter usuário logado via Session
     */
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
                        Log.e("AnuncioRepository", "User vazio na sessão");
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