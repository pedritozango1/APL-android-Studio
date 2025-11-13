package com.example.localizacaoloq.Repository;
import com.example.localizacaoloq.model.User;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserRepository extends  ApiReposistory{
    public String registerUser(User user) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(baseUrl + "/users/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Corpo da requisição
            JSONObject json = new JSONObject();
            json.put("username", user.getUsername());
            json.put("password", user.getPassword());

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

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            if (responseCode >= 200 && responseCode < 300) {
                return "Sucesso: " + response.toString();
            } else {
                return "Erro (" + responseCode + "): " + response.toString();
            }

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}
