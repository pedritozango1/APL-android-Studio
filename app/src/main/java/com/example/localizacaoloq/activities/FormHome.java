package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AnuncioRepository;
import com.example.localizacaoloq.Repository.AuthRepository;
import com.example.localizacaoloq.adapter.AnuncioAdapter;
import com.example.localizacaoloq.model.Anuncio;
import com.example.localizacaoloq.model.Config;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.model.User;
import com.example.localizacaoloq.utils.NavBarHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FormHome extends AppCompatActivity {
    private ImageView nav_locais_icon;
    private User usuario;
    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private ImageButton configButton;
    private AnuncioRepository anuncioRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        configButton=findViewById(R.id.btnSettings);
        // Inicializar repository
        anuncioRepo = AnuncioRepository.getInstance();
        anuncioRepo.setContext(getApplicationContext());

        // Carregar usuário e anúncios
        carregarUsuarioEAnuncios();

        // Configurar navegação
        NavBarHelper.setup(this);
        nav_locais_icon = findViewById(R.id.nav_locais_icon);
        nav_locais_icon.setOnClickListener(v -> startFormLocais());

        // Configurar FAB
        FloatingActionButton fab = findViewById(R.id.fab_add);
        if (fab != null) {
            fab.setOnClickListener(v -> startAnuncio());
        } else {
            Log.e("FormHome", "FAB não encontrado!");
        }
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarConfigracao();
            }
        });
        // Configurar RecyclerView
        setupRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar anúncios quando voltar para a tela
        if (usuario != null) {
            listarAnuncioUtilizador();
        }
    }

    private void carregarUsuarioEAnuncios() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String sessionId = sessionManager.getSessionId();

        if (sessionId != null && !sessionId.isEmpty()) {
            new Thread(() -> {
                try {
                    AuthRepository authRepo = new AuthRepository();
                    Session session = authRepo.pegarIdSessao(sessionId);

                    if (session != null && session.isActive()) {
                        usuario = session.getUser();

                        runOnUiThread(() -> {
                            if (usuario != null) {
                                Log.d("FormHome", "Usuário carregado: " + usuario.getUsername());
                                listarAnuncioUtilizador();
                            } else {
                                Toast.makeText(this, "Erro ao carregar usuário", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
                            // Redirecionar para tela de login se necessário
                        });
                    }
                } catch (Exception e) {
                    Log.e("FormHome", "Erro ao carregar usuário: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "Nenhuma sessão ativa. Faça login.", Toast.LENGTH_LONG).show();
            // Redirecionar para tela de login se necessário
        }
    }

    private void setupRecycler() {
        recyclerView = findViewById(R.id.recyclerAnuncios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar com lista vazia
        adapter = new AnuncioAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void listarAnuncioUtilizador() {
        if (usuario == null || usuario.get_id() == null || usuario.get_id().isEmpty()) {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        String idUsuario = usuario.get_id();
        Log.d("FormHome", "Buscando anúncios do usuário: " + idUsuario);

        new Thread(() -> {
            try {
                List<Anuncio> anuncios = anuncioRepo.findAnuncioUtilizador(idUsuario);

                runOnUiThread(() -> {
                    if (anuncios != null && !anuncios.isEmpty()) {
                        adapter.updateLista(anuncios);
                        Log.d("FormHome", "Anúncios carregados: " + anuncios.size());
                    } else {
                        adapter.updateLista(new ArrayList<>());
                        Toast.makeText(this, "Você ainda não tem anúncios", Toast.LENGTH_SHORT).show();
                        Log.d("FormHome", "Nenhum anúncio encontrado");
                    }
                });

            } catch (Exception e) {
                Log.e("FormHome", "Erro ao buscar anúncios: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao carregar anúncios", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void startFormLocais() {
        Intent formLocal = new Intent(this, LocalForm.class);
        startActivity(formLocal);
    }

    private void startAnuncio() {
        Intent init = new Intent(this, FormAnuncio.class);
        startActivity(init);
    }
    private void iniciarConfigracao(){
        Intent iniConfig=new Intent(this,configuracao.class);
        startActivity(iniConfig);
    }
}