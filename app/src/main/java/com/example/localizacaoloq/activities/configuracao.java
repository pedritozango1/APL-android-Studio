package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AuthRepository;
import com.example.localizacaoloq.Repository.ConfigRepository;
import com.example.localizacaoloq.Repository.UserRepository;
import com.example.localizacaoloq.model.SessionManager;

public class configuracao extends AppCompatActivity {

    private LinearLayout buttonCardLogout;
    private String idSessa;
    private String userId;

    // Switches do layout
    private SwitchCompat switchModoMula;
    private SwitchCompat switchWifiDirect;
    private SwitchCompat switchNotificacoes;

    // Repository de configurações
    private ConfigRepository configRepository;
    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracao);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obter ID da sessão
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        idSessa = sessionManager.getSessionId();

        // Verificar se há sessão ativa
        if (idSessa == null || idSessa.isEmpty()) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obter ID do usuário do UserRepository
        try {
            userRepo = new UserRepository();
            userId = userRepo.getIntance().get_id();

            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao obter informações do usuário", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar repository de configurações
        configRepository = new ConfigRepository(this);

        // Inicializar componentes do layout
        inicializarComponentes();

        // Carregar configurações salvas para este usuário
        carregarConfiguracoes();

        // Configurar listeners
        configurarListeners();

        // Configurar botão voltar
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Configurar botão terminar sessão
        buttonCardLogout = findViewById(R.id.card_logout);
        buttonCardLogout.setOnClickListener(v -> terminarSessao());
    }

    private void inicializarComponentes() {
        // Inicializar switches
        switchModoMula = findViewById(R.id.switch_modo_mula);
        switchWifiDirect = findViewById(R.id.switch_wifi_direct);
        switchNotificacoes = findViewById(R.id.switch_notifications);
    }

    private void carregarConfiguracoes() {
        try {
            // Carregar configurações específicas para este usuário
            boolean modoMula = configRepository.getModoMula(userId);
            boolean wifiDirect = configRepository.getWifiDirect(userId);
            boolean notificacoes = configRepository.getNotificacoes(userId);

            // Aplicar valores aos switches
            switchModoMula.setChecked(modoMula);
            switchWifiDirect.setChecked(wifiDirect);
            switchNotificacoes.setChecked(notificacoes);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar configurações", Toast.LENGTH_SHORT).show();

            // Valores padrão em caso de erro
            switchModoMula.setChecked(false);
            switchWifiDirect.setChecked(false);
            switchNotificacoes.setChecked(true);
        }
    }

    private void configurarListeners() {
        // Listener para Modo Mula
        switchModoMula.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean sucesso = configRepository.atualizarModoMula(userId, isChecked);
                if (sucesso) {
                    String mensagem = "Modo Mula " + (isChecked ? "ativado" : "desativado");
                    Toast.makeText(configuracao.this, mensagem, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(configuracao.this, "Erro ao salvar configuração", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener para WiFi-Direct
        switchWifiDirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean sucesso = configRepository.atualizarWifiDirect(userId, isChecked);
                if (sucesso) {
                    String mensagem = "WiFi-Direct " + (isChecked ? "ativado" : "desativado");
                    Toast.makeText(configuracao.this, mensagem, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(configuracao.this, "Erro ao salvar configuração", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener para Notificações
        switchNotificacoes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean sucesso = configRepository.atualizarNotificacoes(userId, isChecked);
                if (sucesso) {
                    String mensagem = "Notificações " + (isChecked ? "ativadas" : "desativadas");
                    Toast.makeText(configuracao.this, mensagem, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(configuracao.this, "Erro ao salvar configuração", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void terminarSessao() {
        if (idSessa != null && !idSessa.isEmpty()) {
            // Mostrar loading
            Toast.makeText(configuracao.this, "Terminando sessão...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                try {
                    AuthRepository authrep = new AuthRepository();
                    String session = authrep.logout(idSessa);

                    runOnUiThread(() -> {
                        if (session != null) {
                            // Limpar sessão local
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            sessionManager.clearSession();

                            Toast.makeText(configuracao.this, "Sessão terminada com sucesso", Toast.LENGTH_SHORT).show();

                            // Fechar todas as activities e ir para MainActivity
                            Intent intent = new Intent(configuracao.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(configuracao.this, "Erro ao terminar sessão no servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(configuracao.this,
                                "Erro de conexão ao terminar sessão: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "Nenhuma sessão ativa", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fechar conexão com o banco de configurações
        if (configRepository != null) {
            configRepository.close();
        }
    }
}