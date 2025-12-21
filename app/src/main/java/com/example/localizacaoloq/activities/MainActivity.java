package com.example.localizacaoloq.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AuthRepository;
import com.example.localizacaoloq.model.Auth;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.model.User;
import com.example.localizacaoloq.Repository.UserRepository;
import com.example.localizacaoloq.service.ReportalLocalizacaoService;

public class MainActivity extends AppCompatActivity {
    private Button btnLogar;
    private Button btnCriar;
    private Auth auth;
    private ReportalLocalizacaoService localizacaoService;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Solicitar permissões de localização
        solicitarPermissoesLocalizacao();

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String id = sessionManager.getSessionId();

        if(!id.isEmpty()){
            new Thread(() -> {
                AuthRepository authrep = new AuthRepository();
                Session session = authrep.pegarIdSessao(id);
                if (session != null && session.isActive()) {
                    runOnUiThread(() ->{
                        // Inicializar e mostrar localização
                        localizacaoService = new ReportalLocalizacaoService(this);
                        mostrarLocalizacaoAtual();
                        localizacaoService.iniciarMonitoramento();
                        startHome();
                    });
                }
            }).start();
        }

        btnCriar = findViewById(R.id.btnConta);
        btnLogar = findViewById(R.id.btnLoginSistema);

        btnCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCriarConta();
            }
        });

        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etUsername = findViewById(R.id.etUsername);
                EditText etPassword = findViewById(R.id.etPassword);

                String nome = etUsername.getText().toString().trim();
                String senha = etPassword.getText().toString();

                if (nome.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                User utilizador = new User(nome, senha);
                AuthRepository authRepository = new AuthRepository();

                new Thread(() -> {
                    Session auth = authRepository.login(utilizador);
                    runOnUiThread(() -> {
                        if (auth != null && auth.getSessionId() != null) {
                            // Salvar sessionId localmente
                            SessionManager sessionManager = new SessionManager(MainActivity.this);
                            sessionManager.saveSession(auth.getSessionId());

                            // Mostrar localização após login
                            mostrarLocalizacaoAtual();

                            // Iniciar serviço de localização
                            localizacaoService = new ReportalLocalizacaoService(MainActivity.this);
                            localizacaoService.iniciarMonitoramento();

                            Toast.makeText(MainActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                            // Ir para HomeActivity
                            startHome();
                        } else {
                            Toast.makeText(MainActivity.this, "Credenciais inválidas!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });

        // Mostrar localização ao iniciar o app
        mostrarLocalizacaoAtual();
    }

    private void solicitarPermissoesLocalizacao() {
        // Verificar se já tem permissões
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permissões
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            // Já tem permissão, pode usar a localização
            Toast.makeText(this, "Permissão de localização já concedida", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarLocalizacaoAtual() {
        new Thread(() -> {
            try {
                // Aguardar um pouco para garantir que o GPS está pronto
                Thread.sleep(1000);

                Location location = obterLocalizacaoAtual();

                runOnUiThread(() -> {
                    if (location != null) {
                        String mensagem = String.format(
                                "📍 Localização atual:\nLat: %.6f\nLon: %.6f\nPrecisão: %.1fm",
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAccuracy()
                        );

                        Toast.makeText(MainActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        Log.d("MainActivity", mensagem);

                        // Também mostrar no Logcat para debug
                        Log.i("LOCALIZACAO",
                                String.format("Latitude: %.6f, Longitude: %.6f, Provider: %s",
                                        location.getLatitude(),
                                        location.getLongitude(),
                                        location.getProvider()
                                )
                        );
                    } else {
                        Toast.makeText(MainActivity.this,
                                "📍 Localização não disponível.\nVerifique se o GPS está ligado.",
                                Toast.LENGTH_LONG).show();
                        Log.d("MainActivity", "Localização não disponível");
                    }
                });

            } catch (Exception e) {
                Log.e("MainActivity", "Erro ao obter localização: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Erro ao obter localização: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private Location obterLocalizacaoAtual() {
        try {
            // Verificar permissões
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                Log.w("MainActivity", "Sem permissão de localização");
                return null;
            }

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Listar todos os providers disponíveis
            StringBuilder providersInfo = new StringBuilder("Providers disponíveis:\n");
            for (String provider : locationManager.getAllProviders()) {
                boolean enabled = locationManager.isProviderEnabled(provider);
                providersInfo.append(provider).append(": ").append(enabled ? "Ativo" : "Inativo").append("\n");
            }
            Log.d("MainActivity", providersInfo.toString());

            // Tentar obter localização do GPS
            Location location = null;

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                try {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        Log.d("MainActivity", "Localização obtida do GPS");
                    }
                } catch (SecurityException e) {
                    Log.e("MainActivity", "Erro de permissão ao acessar GPS: " + e.getMessage());
                }
            }

            // Se GPS não estiver disponível, tentar rede
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                try {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        Log.d("MainActivity", "Localização obtida da Rede");
                    }
                } catch (SecurityException e) {
                    Log.e("MainActivity", "Erro de permissão ao acessar Rede: " + e.getMessage());
                }
            }

            // Se ainda não tiver, tentar localização passiva
            if (location == null) {
                try {
                    location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    if (location != null) {
                        Log.d("MainActivity", "Localização obtida do Passive Provider");
                    }
                } catch (SecurityException e) {
                    Log.e("MainActivity", "Erro de permissão ao acessar Passive Provider: " + e.getMessage());
                }
            }

            return location;

        } catch (Exception e) {
            Log.e("MainActivity", "Erro no obterLocalizacaoAtual: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permissão de localização concedida!", Toast.LENGTH_SHORT).show();
                // Recarregar localização
                mostrarLocalizacaoAtual();

                // Iniciar serviço se estiver logado
                if (localizacaoService != null) {
                    localizacaoService.iniciarMonitoramento();
                }
            } else {
                Toast.makeText(this,
                        "❌ Permissão de localização negada.\nO app não funcionará corretamente.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualizar localização quando a activity voltar ao foco
        mostrarLocalizacaoAtual();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Parar serviço quando a activity for destruída
        if (localizacaoService != null) {
            localizacaoService.pararMonitoramento();
        }
    }

    private void startCriarConta(){
        Intent navegar = new Intent(this, CriarConta.class);
        startActivity(navegar);
    }

    private void startHome(){
        Intent intent = new Intent(MainActivity.this, FormHome.class);
        startActivity(intent);
    }
}