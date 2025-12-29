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
import com.example.localizacaoloq.Repository.ConfigRepository;
import com.example.localizacaoloq.model.Auth;
import com.example.localizacaoloq.model.Perfil;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.model.User;
import com.example.localizacaoloq.Repository.UserRepository;
import com.example.localizacaoloq.service.ReportalLocalizacaoService;

public class MainActivity extends AppCompatActivity {
    private Button btnLogar;
    private Button btnCriar;
    private Auth auth;
    private User user;
    private UserRepository userRepo;
    private ConfigRepository configRepository;
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

        // Inicializar repositório de configurações
        configRepository = new ConfigRepository(this);

        // Solicitar permissões de localização
        solicitarPermissoesLocalizacao();

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String id = sessionManager.getSessionId();

        // Log para debug
        Log.d("MainActivity", "Session ID no onCreate: " + (id != null ? id : "null"));

        if (id != null && !id.isEmpty()) {
            userRepo = new UserRepository();
            new Thread(() -> {
                try {
                    AuthRepository authrep = new AuthRepository();
                    Session session = authrep.pegarIdSessao(id);

                    runOnUiThread(() -> {
                        if (session != null && session.isActive()) {
                            Log.d("MainActivity", "Sessão válida encontrada");
                            iniciarUtilizador(session);

                            // VERIFICAR E CRIAR CONFIGURAÇÕES DO USUÁRIO
                            verificarECriarConfiguracoesUsuario();

                            // Inicializar e mostrar localização
                            localizacaoService = new ReportalLocalizacaoService(this);
                            mostrarLocalizacaoAtual();
                            localizacaoService.iniciarMonitoramento();
                            startHome();
                        } else {
                            // Sessão inválida ou expirada - limpar sessão local
                            Log.d("MainActivity", "Sessão inválida ou expirada");
                            sessionManager.clearSession();

                            // Mostrar mensagem informativa
                            if (session == null) {
                                Toast.makeText(MainActivity.this,
                                        "Sessão expirada. Faça login novamente.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Sessão inativa. Faça login novamente.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("MainActivity", "Erro ao verificar sessão: " + e.getMessage());
                    runOnUiThread(() -> {
                        sessionManager.clearSession();
                        Toast.makeText(MainActivity.this,
                                "Erro ao verificar sessão. Faça login novamente.",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } else {
            Log.d("MainActivity", "Nenhuma sessão salva encontrada");
            // Mostrar tela de login normalmente
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
                realizarLogin();
            }
        });

        // Mostrar localização ao iniciar o app
        mostrarLocalizacaoAtual();
    }

    // MÉTODO SEPARADO PARA REALIZAR LOGIN
    private void realizarLogin() {
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
            try {
                Session auth = authRepository.login(utilizador);
                runOnUiThread(() -> {
                    if (auth != null && auth.getSessionId() != null) {
                        // Salvar sessionId localmente
                        SessionManager sessionManager = new SessionManager(MainActivity.this);
                        sessionManager.saveSession(auth.getSessionId());

                        // Inicializar usuário após login
                        userRepo = new UserRepository();
                        userRepo.getIntance().set_id(auth.getUser().get_id());
                        userRepo.getIntance().setUsername(auth.getUser().getUsername());
                        for (Perfil perfil : auth.getUser().getPerfil()) {
                            userRepo.getIntance().addPerfil(perfil);
                        }
                        userRepo.getIntance().setPassword(auth.getUser().getPassword());

                        // VERIFICAR E CRIAR CONFIGURAÇÕES DO USUÁRIO APÓS LOGIN
                        verificarECriarConfiguracoesUsuario();

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
            } catch (Exception e) {
                Log.e("MainActivity", "Erro no login: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Erro ao conectar com o servidor. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // MÉTODO PARA VERIFICAR E CRIAR CONFIGURAÇÕES DO USUÁRIO
    private void verificarECriarConfiguracoesUsuario() {
        try {
            // Verificar se userRepo foi inicializado
            if (userRepo == null || userRepo.getIntance() == null) {
                Log.w("MainActivity", "UserRepository não inicializado");
                return;
            }

            // Obter ID do usuário do UserRepository
            String userId = userRepo.getIntance().get_id();

            if (userId == null || userId.isEmpty()) {
                Log.w("MainActivity", "ID do usuário não encontrado");
                return;
            }

            Log.d("MainActivity", "Verificando configurações para usuário: " + userId);

            // Verificar se existem configurações para este usuário
            if (!configRepository.existeConfiguracoesParaUsuario(userId)) {
                // Criar configurações padrão para o usuário
                boolean criado = configRepository.criarConfiguracoesPadraoUsuario(userId);
                if (criado) {
                    Log.d("MainActivity", "Configurações padrão criadas para usuário: " + userId);
                } else {
                    Log.e("MainActivity", "Erro ao criar configurações para usuário: " + userId);
                }
            } else {
                Log.d("MainActivity", "Configurações já existem para usuário: " + userId);
            }

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao verificar/criar configurações: " + e.getMessage());
            e.printStackTrace();
        }
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
            Log.d("MainActivity", "Permissão de localização já concedida");
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

                        Log.d("MainActivity", mensagem);

                        // Mostrar Toast apenas se não estiver indo para Home
                        if (btnLogar.getVisibility() == View.VISIBLE) {
                            Toast.makeText(MainActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("MainActivity", "Localização não disponível");
                    }
                });

            } catch (Exception e) {
                Log.e("MainActivity", "Erro ao obter localização: " + e.getMessage());
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
                Log.d("MainActivity", "✅ Permissão de localização concedida!");
                // Recarregar localização
                mostrarLocalizacaoAtual();

                // Iniciar serviço se estiver logado
                if (localizacaoService != null) {
                    localizacaoService.iniciarMonitoramento();
                }
            } else {
                Log.w("MainActivity", "❌ Permissão de localização negada");
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

        // Fechar conexão com o banco de configurações
        if (configRepository != null) {
            configRepository.close();
        }
    }

    private void startCriarConta() {
        Intent navegar = new Intent(this, CriarConta.class);
        startActivity(navegar);
    }

    private void startHome() {
        Intent intent = new Intent(MainActivity.this, FormHome.class);
        startActivity(intent);
        finish(); // Fechar MainActivity para não voltar com back button
    }

    private void iniciarUtilizador(Session session) {
        if (session != null && session.getUser() != null) {
            userRepo.getIntance();
            userRepo.getIntance().set_id(session.getUser().get_id());
            userRepo.getIntance().setUsername(session.getUser().getUsername());
            for (Perfil perfil : session.getUser().getPerfil()) {
                userRepo.getIntance().addPerfil(perfil);
            }
            userRepo.getIntance().setPassword(session.getUser().getPassword());
            Log.d("MainActivity", "Usuário inicializado: " + session.getUser().getUsername());
        }
    }
}