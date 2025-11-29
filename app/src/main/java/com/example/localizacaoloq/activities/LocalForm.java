package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.LocalRepository;
import com.example.localizacaoloq.adapter.AdapterLocal;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.utils.NavBarHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalForm extends AppCompatActivity {
    private RecyclerView rv;
    private LocalRepository repo;
    private AdapterLocal adapter;
    private TextView tvContador;
    private ImageButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_local_form);

        // ✅ Configurar insets do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // bottom = 0 porque navbar já lida com isso
            return insets;
        });

        // Inicializar Repository
        repo = LocalRepository.getInstance();

        // Inicializar Views
        rv = findViewById(R.id.recyclerViewLocais);
        tvContador = findViewById(R.id.tvContador);
        btnAdd = findViewById(R.id.btnAdd);

        // Configurar Adapter
        adapter = new AdapterLocal(new ArrayList<>(), this::deletarLocal);

        // Configurar RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Configurar botão adicionar
        btnAdd.setOnClickListener(v -> startRegistarLocal());

        // Carregar dados
        carregarLocais();
        atualizarContador();

        // Inicializar NavBarHelper
        NavBarHelper.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Usa o cache se já estiver carregado
        if (repo.isCacheCarregado()) {
            adapter.updateLocais(repo.getLocais());
            atualizarContador();
        } else {
            carregarLocais();
        }
    }

    private void deletarLocal(Local local, int position) {
        new Thread(() -> {
            boolean success = repo.delete(local.get_id());

            runOnUiThread(() -> {
                if (success) {
                    // ✅ Atualiza a UI com o cache já atualizado
                    adapter.updateLocais(repo.getLocais());
                    atualizarContador();
                    Toast.makeText(this, "Local deletado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao deletar local!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void atualizarContador() {
        int count = repo.getLocais().size();
        tvContador.setText(count + " local(is) disponível(is)");
    }

    private void startRegistarLocal() {
        startActivity(new Intent(this, FormLocalRegistado.class));
    }

    private void carregarLocais() {
        new Thread(() -> {
            // ✅ Lista usa cache se disponível, senão busca do servidor
            List<Local> lista = repo.findAll();

            runOnUiThread(() -> {
                adapter.updateLocais(lista);
                atualizarContador();
            });
        }).start();
    }
}