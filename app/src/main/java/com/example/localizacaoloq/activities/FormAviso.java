package com.example.localizacaoloq.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AnuncioRepository;
import com.example.localizacaoloq.model.Anuncio;
import com.example.localizacaoloq.model.User;
import com.example.localizacaoloq.service.ReportalLocalizacaoService;
import com.example.localizacaoloq.utils.NavBarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormAviso extends AppCompatActivity {

    private AnuncioRepository anuncioRepo;
    private ReportalLocalizacaoService localizacaoService;
    private LinearLayout containerNotificacoes;
    private TextView txtSemNotificacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_aviso);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavBarHelper.setup(this);
        inicializarViews();
        inicializarRepositorios();
        inicializarServicoLocalizacao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar notificações quando a activity voltar ao foco
        carregarNotificacoes();
        // Iniciar monitoramento de localização
        if (localizacaoService != null) {
            localizacaoService.iniciarMonitoramento();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Parar monitoramento quando a activity não estiver em foco
        if (localizacaoService != null) {
            localizacaoService.pararMonitoramento();
        }
    }

    private void inicializarViews() {
        // Encontrar o container principal dentro do ScrollView
        LinearLayout scrollContent = findViewById(R.id.scroll_content);

        containerNotificacoes = findViewById(R.id.container_notificacoes);
        txtSemNotificacoes = findViewById(R.id.txt_sem_notificacoes);

        // Se não existirem, criar programaticamente
        if (containerNotificacoes == null && scrollContent != null) {
            containerNotificacoes = new LinearLayout(this);
            containerNotificacoes.setId(R.id.container_notificacoes);
            containerNotificacoes.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 0);
            containerNotificacoes.setLayoutParams(params);
            scrollContent.addView(containerNotificacoes);
        }

        if (txtSemNotificacoes == null && scrollContent != null) {
            txtSemNotificacoes = new TextView(this);
            txtSemNotificacoes.setId(R.id.txt_sem_notificacoes);
            txtSemNotificacoes.setText("Nenhuma notificação no momento");
            txtSemNotificacoes.setTextSize(18);
            txtSemNotificacoes.setTextColor(getResources().getColor(android.R.color.darker_gray));
            txtSemNotificacoes.setGravity(android.view.Gravity.CENTER);
            txtSemNotificacoes.setPadding(0, 100, 0, 0);
            scrollContent.addView(txtSemNotificacoes, 0);
        }
    }

    private void inicializarRepositorios() {
        anuncioRepo = AnuncioRepository.getInstance();
        anuncioRepo.setContext(getApplicationContext());
    }

    private void inicializarServicoLocalizacao() {
        localizacaoService = new ReportalLocalizacaoService(this);
    }

    private void carregarNotificacoes() {
        new Thread(() -> {
            try {
                // Primeiro, tentar obter localização atual
                Location location = obterLocalizacaoAtual();
                List<String> ssids = obterSSIDsWiFi();

                // Obter usuário logado
                User usuario = anuncioRepo.getCurrentUser();

                List<Anuncio> anuncios;

                if (usuario != null && location != null) {
                    // Buscar anúncios próximos baseados na localização
                    anuncios = anuncioRepo.getAnunciosProximosPorLocalizacao(
                            location.getLatitude(),
                            location.getLongitude(),
                            usuario.getUsername(),
                            ssids
                    );

                    Log.d("FormAviso", "Anúncios próximos encontrados: " + (anuncios != null ? anuncios.size() : 0));

                    if (anuncios == null || anuncios.isEmpty()) {
                        // Se não encontrar anúncios próximos, buscar todos
                        anuncios = anuncioRepo.findAll();
                        Log.d("FormAviso", "Buscando todos os anúncios: " + (anuncios != null ? anuncios.size() : 0));
                    }
                } else {

                    anuncios = anuncioRepo.findAll();
                    Log.d("FormAviso", "Usuário não logado ou sem localização. Buscando todos: " + (anuncios != null ? anuncios.size() : 0));

                }

                final List<Anuncio> finalAnuncios = anuncios;
                runOnUiThread(() -> {
                    if (finalAnuncios != null && !finalAnuncios.isEmpty()) {
                        mostrarNotificacoes(finalAnuncios);
                    } else {
                        mostrarMensagemSemNotificacoes();
                        // Para testes, adicionar notificações de exemplo
                        adicionarNotificacoesExemplo();
                    }
                });

            } catch (Exception e) {
                Log.e("FormAviso", "Erro ao carregar notificações: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao carregar notificações", Toast.LENGTH_SHORT).show();
                    // Em caso de erro, mostrar exemplos
                    adicionarNotificacoesExemplo();
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
                return null;
            }

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Tentar GPS primeiro
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                // Tentar rede
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            return location;

        } catch (Exception e) {
            Log.e("FormAviso", "Erro ao obter localização: " + e.getMessage());
            return null;
        }
    }

    private List<String> obterSSIDsWiFi() {
        List<String> ssids = new ArrayList<>();
        try {
            // Verificar permissão (Android 10+ requer ACCESS_FINE_LOCATION para WiFi scan)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return ssids;
            }

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                List<ScanResult> resultados = wifiManager.getScanResults();
                for (ScanResult resultado : resultados) {
                    ssids.add(resultado.SSID);
                }
            }

        } catch (Exception e) {
            Log.e("FormAviso", "Erro ao obter SSIDs WiFi: " + e.getMessage());
        }

        return ssids;
    }

    private void mostrarNotificacoes(List<Anuncio> anuncios) {
        // Limpar container
        if (containerNotificacoes != null) {
            containerNotificacoes.removeAllViews();
        }

        // Esconder mensagem "sem notificações"
        if (txtSemNotificacoes != null) {
            txtSemNotificacoes.setVisibility(View.GONE);
        }

        // Adicionar cada anúncio como uma notificação
        for (Anuncio anuncio : anuncios) {
            adicionarNotificacaoCard(anuncio);
        }

        // Se ainda não houver notificações, mostrar mensagem
        if (containerNotificacoes.getChildCount() == 0) {
            mostrarMensagemSemNotificacoes();
        }
    }

    private void adicionarNotificacaoCard(Anuncio anuncio) {
        try {
            // Inflar o layout do card
            View cardView = LayoutInflater.from(this)
                    .inflate(R.layout.item_notificacao_card, containerNotificacoes, false);

            // Configurar dados do anúncio
            TextView txtTitulo = cardView.findViewById(R.id.txt_titulo_anuncio);
            TextView txtDescricao = cardView.findViewById(R.id.txt_descricao_anuncio);
            TextView txtLocal = cardView.findViewById(R.id.txt_local_anuncio);
            TextView txtData = cardView.findViewById(R.id.txt_data_anuncio);
            TextView txtTagNovo = cardView.findViewById(R.id.txt_tag_novo);
            TextView badgeLocal = cardView.findViewById(R.id.badge_local);
            TextView badgeModoEntrega = cardView.findViewById(R.id.badge_modo_entrega);

            // Título
            if (anuncio.getTitulo() != null && !anuncio.getTitulo().isEmpty()) {
                txtTitulo.setText(anuncio.getTitulo());
            } else {
                txtTitulo.setText("Anúncio sem título");
            }

            // Descrição
            if (anuncio.getMensagem() != null && !anuncio.getMensagem().isEmpty()) {
                txtDescricao.setText(anuncio.getMensagem());
            } else {
                txtDescricao.setText("Sem descrição");
            }

            // Local
            if (anuncio.getLocal() != null && anuncio.getLocal().getNome() != null) {
                txtLocal.setText(anuncio.getLocal().getNome());

                // Mostrar tipo do local como badge
                if (anuncio.getLocal().getTipo() != null) {
                    badgeLocal.setText(anuncio.getLocal().getTipo().toUpperCase());
                    badgeLocal.setVisibility(View.VISIBLE);
                }
            } else {
                txtLocal.setText("Local não especificado");
            }

            // Data
            if (anuncio.getInicio() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                txtData.setText(sdf.format(anuncio.getInicio()));
            } else {
                txtData.setText("Data não especificada");
            }

            // Tag "NOVO" (se for recente)
            if (anuncio.getCreatedAt() != null) {
                long diferenca = new Date().getTime() - anuncio.getCreatedAt().getTime();
                long horas = diferenca / (60 * 60 * 1000);

                if (horas < 24) { // Nas últimas 24 horas
                    txtTagNovo.setVisibility(View.VISIBLE);
                }
            }

            // Modo de entrega
            if (anuncio.getModoEntrega() != null) {
                String modo = anuncio.getModoEntrega().toUpperCase();
                badgeModoEntrega.setText(modo);

                // Cores diferentes para cada modo
                if (modo.equals("CENTRALIZADO")) {
                    try {
                        badgeModoEntrega.setTextColor(getResources().getColor(R.color.centralizado));
                        badgeModoEntrega.setBackgroundResource(R.drawable.badge_centralizado_background);
                    } catch (Exception e) {
                        // Fallback se os recursos não existirem
                        badgeModoEntrega.setTextColor(0xFF2196F3);
                        badgeModoEntrega.setBackgroundColor(0xFFE3F2FD);
                    }
                } else if (modo.equals("DESCENTRALIZADO")) {
                    try {
                        badgeModoEntrega.setTextColor(getResources().getColor(R.color.descentralizado));
                        badgeModoEntrega.setBackgroundResource(R.drawable.badge_descentralizado_background);
                    } catch (Exception e) {
                        // Fallback se os recursos não existirem
                        badgeModoEntrega.setTextColor(0xFF4CAF50);
                        badgeModoEntrega.setBackgroundColor(0xFFE8F5E9);
                    }
                }
            }

            // Configurar clique no card
            cardView.setOnClickListener(v -> {
                abrirDetalhesAnuncio(anuncio);
            });

            // Adicionar ao container
            containerNotificacoes.addView(cardView);

        } catch (Exception e) {
            Log.e("FormAviso", "Erro ao criar card de notificação: " + e.getMessage());
        }
    }

    private void adicionarNotificacoesExemplo() {
        // Notificação de exemplo 1
        Anuncio exemplo1 = new Anuncio();
        exemplo1.setTitulo("Jogo na escola");
        exemplo1.setMensagem("Queremos jogar futebol e não encontramos nenhum clube aliciante");
        exemplo1.setInicio(parseDate("05/12/2025 05:00"));
        exemplo1.setCreatedAt(new Date()); // Agora = novo
        exemplo1.setModoEntrega("centralizado");

        com.example.localizacaoloq.model.Local local1 =
                new com.example.localizacaoloq.model.LocalGPS("Talatona", -8.839, 13.289, 100);
        exemplo1.setLocal(local1);

        adicionarNotificacaoCard(exemplo1);

        // Notificação de exemplo 2
        Anuncio exemplo2 = new Anuncio();
        exemplo2.setTitulo("Encontro de programadores");
        exemplo2.setMensagem("Vamos discutir sobre desenvolvimento mobile e APIs REST");
        exemplo2.setInicio(parseDate("10/01/2025 08:00"));
        exemplo2.setCreatedAt(new Date(System.currentTimeMillis() - 48 * 60 * 60 * 1000)); // 2 dias atrás
        exemplo2.setModoEntrega("descentralizado");

        com.example.localizacaoloq.model.Local local2 =
                new com.example.localizacaoloq.model.LocalGPS("Ponto Central", -8.838, 13.290, 50);
        exemplo2.setLocal(local2);

        adicionarNotificacaoCard(exemplo2);

        // Notificação de exemplo 3
        Anuncio exemplo3 = new Anuncio();
        exemplo3.setTitulo("Venda de livros");
        exemplo3.setMensagem("Livros técnicos de programação com 50% de desconto");
        exemplo3.setInicio(parseDate("15/01/2025 10:00"));
        exemplo2.setCreatedAt(new Date(System.currentTimeMillis() - 72 * 60 * 60 * 1000)); // 3 dias atrás
        exemplo3.setModoEntrega("centralizado");

        com.example.localizacaoloq.model.Local local3 =
                new com.example.localizacaoloq.model.LocalGPS("Biblioteca Central", -8.837, 13.288, 30);
        exemplo3.setLocal(local3);

        adicionarNotificacaoCard(exemplo3);

        // Esconder mensagem "sem notificações"
        if (txtSemNotificacoes != null) {
            txtSemNotificacoes.setVisibility(View.GONE);
        }
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }

    private void abrirDetalhesAnuncio(Anuncio anuncio) {
        Toast.makeText(this, "Abrindo: " + anuncio.getTitulo(), Toast.LENGTH_SHORT).show();
        // Implementar navegação para detalhes
    }

    private void mostrarMensagemSemNotificacoes() {
        if (txtSemNotificacoes != null) {
            txtSemNotificacoes.setVisibility(View.VISIBLE);
        }

        if (containerNotificacoes != null) {
            containerNotificacoes.removeAllViews();
        }
    }
}