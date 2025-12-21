package com.example.localizacaoloq.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AnuncioRepository;
import com.example.localizacaoloq.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReportalLocalizacaoService {
    private static final long INTERVALO_LOCALIZACAO = 30000; // 30 segundos
    private static final String CHANNEL_ID = "anuncios_channel";
    private static final int NOTIFICATION_ID = 1;

    private Handler handler;
    private Runnable periodicTask;
    private AnuncioRepository anuncioRepo;
    private Context context;
    private boolean monitoramentoAtivo = false;

    public ReportalLocalizacaoService(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.anuncioRepo = AnuncioRepository.getInstance();
        this.anuncioRepo.setContext(context);

        // Criar canal de notificação
        criarCanalNotificacao();
    }

    public void iniciarMonitoramento() {
        if (monitoramentoAtivo) {
            pararMonitoramento();
        }

        // Verificar permissões antes de iniciar
        if (!verificarPermissoes()) {
            Log.w("LocalizacaoService", "Permissões insuficientes para monitoramento");
            return;
        }

        monitoramentoAtivo = true;
        periodicTask = new Runnable() {
            @Override
            public void run() {
                enviarLocalizacaoAtual();
                if (monitoramentoAtivo) {
                    handler.postDelayed(this, INTERVALO_LOCALIZACAO);
                }
            }
        };

        handler.post(periodicTask);
    }

    public void pararMonitoramento() {
        monitoramentoAtivo = false;
        if (periodicTask != null) {
            handler.removeCallbacks(periodicTask);
            periodicTask = null;
        }
    }

    private boolean verificarPermissoes() {
        // Verificar permissão de localização
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocalizacaoService", "Permissão de localização não concedida");
            return false;
        }

        // Verificar permissão de WiFi (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("LocalizacaoService", "Permissão de localização precisa para WiFi scan");
                return false;
            }
        }

        // Verificar permissão de notificações (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("LocalizacaoService", "Permissão de notificações não concedida");
                // Continuar mesmo sem permissão de notificação
            }
        }

        return true;
    }

    private void enviarLocalizacaoAtual() {
        new Thread(() -> {
            try {
                // 1. Obter localização GPS com verificação de permissão
                LocationManager locationManager =
                        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (location == null) {
                    Log.d("LocalizacaoService", "Localização não disponível");
                    return;
                }

                // 2. Obter WiFi visível
                List<String> ssids = new ArrayList<>();
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    WifiManager wifiManager =
                            (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    if (wifiManager != null && wifiManager.isWifiEnabled()) {
                        List<ScanResult> resultados = wifiManager.getScanResults();
                        for (ScanResult resultado : resultados) {
                            ssids.add(resultado.SSID);
                        }
                    }
                }

                // 3. Obter usuário logado
                User usuario = anuncioRepo.getCurrentUser();
                if (usuario == null) {
                    Log.d("LocalizacaoService", "Usuário não logado");
                    return;
                }

                // 4. Enviar para servidor
                JSONObject json = new JSONObject();
                json.put("latitude", location.getLatitude());
                json.put("longitude", location.getLongitude());
                json.put("ssids", new JSONArray(ssids));
                json.put("username", usuario.getUsername());

                // Chamar endpoint /localizacao/reportar
                String resposta = fazerRequisicaoPOST(
                        "http://10.0.2.2:3000/Repostarlocalizacao/reportar", // ← SUBSTITUA PELO SEU IP
                        json.toString()
                );

                if (resposta != null) {
                    // 5. Processar resposta
                    JSONObject respostaJson = new JSONObject(resposta);
                    JSONArray anunciosArray = respostaJson.getJSONArray("anuncios");

                    if (anunciosArray.length() > 0) {
                        // Mostrar notificação para o usuário
                        mostrarNotificacao(anunciosArray.length());
                        Log.d("LocalizacaoService", "Encontrados " + anunciosArray.length() + " anúncios");
                    }
                }

            } catch (Exception e) {
                Log.e("LocalizacaoService", "Erro ao enviar localização: " + e.getMessage());
            }
        }).start();
    }

    private String fazerRequisicaoPOST(String urlString, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            // Enviar corpo da requisição
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Ler resposta
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                Log.e("LocalizacaoService", "Erro HTTP: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            Log.e("LocalizacaoService", "Erro na requisição: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Anúncios";
            String description = "Canal para notificações de anúncios próximos";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void mostrarNotificacao(int quantidadeAnuncios) {
        try {
            // Verificar permissão de notificação (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w("LocalizacaoService", "Sem permissão para mostrar notificação");
                    return;
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications) // ← Certifique-se que este drawable existe
                    .setContentTitle("Anúncios próximos")
                    .setContentText("Há " + quantidadeAnuncios + " anúncios disponíveis na sua localização")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (Exception e) {
            Log.e("LocalizacaoService", "Erro ao mostrar notificação: " + e.getMessage());
        }
    }

    public boolean isMonitoramentoAtivo() {
        return monitoramentoAtivo;
    }
}