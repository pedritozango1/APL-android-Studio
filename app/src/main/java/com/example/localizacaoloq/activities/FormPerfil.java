package com.example.localizacaoloq.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AuthRepository;
import com.example.localizacaoloq.Repository.PerfilReposistory;
import com.example.localizacaoloq.model.Perfil;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.utils.NavBarHelper;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.adapter.AtributoAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
public class FormPerfil extends AppCompatActivity {
    private String idUser;
    private TextInputEditText inputChave, inputValor;
    private PerfilReposistory repo;
    private Button btnAdicionar;
    private RecyclerView recycler;
    private AtributoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Repository
        repo = PerfilReposistory.getInstance();

        // Inicializar Views
        recycler = findViewById(R.id.recyclerAtributos);
        inputChave = findViewById(R.id.input_chave);
        inputValor = findViewById(R.id.input_valor);
        btnAdicionar = findViewById(R.id.btn_adicionar_atributo);

        // Configurar Adapter
        adapter = new AtributoAdapter(new ArrayList<>(), this::deletarPerfil);

        // Configurar RecyclerView
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Configurar botão
        btnAdicionar.setOnClickListener(v -> adicionarAtributo());

        // Carregar sessão do usuário
        carregarSessao();

        NavBarHelper.setup(this);
    }

    private void carregarSessao() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String id = sessionManager.getSessionId();

        if (!id.isEmpty()) {
            new Thread(() -> {
                AuthRepository authrep = new AuthRepository();
                Session session = authrep.pegarIdSessao(id);

                if (session != null && session.isActive() && session.getUser() != null) {
                    idUser = session.getUser().get_id();

                    runOnUiThread(() -> {
                        TextView perfilInitials = findViewById(R.id.perfil_initials);
                        TextView perfilId = findViewById(R.id.perfil_id);

                        String username = session.getUser().getUsername();
                        String initials = username.length() >= 2 ?
                                username.substring(0, 2).toUpperCase() :
                                username.toUpperCase();

                        perfilInitials.setText(initials);
                        perfilId.setText("ID: " + session.getUser().get_id());

                        // ✅ Carregar perfis após ter o ID do usuário
                        carregarPerfis();
                    });
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repo.isCacheCarregado()) {
            adapter.updatePerfis(repo.getPerfis());
        }
    }

    private void deletarPerfil(Perfil perfil, int position) {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "Erro: usuário não identificado!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean success = repo.removePerfil(idUser, perfil.get_id());

            runOnUiThread(() -> {
                if (success) {
                    // ✅ Atualiza a UI com o cache já atualizado
                    adapter.updatePerfis(repo.getPerfis());
                    Toast.makeText(this, "Atributo deletado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao deletar atributo!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void carregarPerfis() {
        if (idUser == null || idUser.isEmpty()) {
            return;
        }

        new Thread(() -> {
            List<Perfil> lista = repo.listarPerfisUsuario(idUser);

            runOnUiThread(() -> {
                adapter.updatePerfis(lista);
            });
        }).start();
    }

    private void adicionarAtributo() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "Erro: usuário não identificado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = inputChave.getText().toString().trim();
        String value = inputValor.getText().toString().trim();

        if (key.isEmpty() || value.isEmpty()) {
            Toast.makeText(this, "Preencha chave e valor!", Toast.LENGTH_SHORT).show();
            return;
        }

        Perfil novoPerfil = new Perfil(key, value);

        new Thread(() -> {
            Perfil perfilCriado = repo.addPerfil(idUser, novoPerfil);
            runOnUiThread(() -> {
                if (perfilCriado != null) {
                    inputChave.setText("");
                    inputValor.setText("");

                    // ✅ Atualiza a UI com o cache já atualizado
                    adapter.updatePerfis(repo.getPerfis());
                    Toast.makeText(this, "Atributo adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao adicionar atributo!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}