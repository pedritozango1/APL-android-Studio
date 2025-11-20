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

        rv = findViewById(R.id.recyclerViewLocais);
        repo = LocalRepository.getInstance();
        adapter = new AdapterLocal(new ArrayList<>(), new AdapterLocal.OnLocalDeleteListener() {
            @Override
            public void onLocalDeleted(Local local, int position) {
                deletarLocal(local, position);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        carregarLocais();

        tvContador = findViewById(R.id.tvContador);
        atualizarContador();

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> startRegistarLocal());
    }

    private void deletarLocal(Local local, int position) {
       new Thread(() -> {
            boolean success = repo.delete(local.get_id());
            runOnUiThread(() -> {
                if (success) {
                    adapter.removeItem(position);
                    atualizarContador();
                    Toast.makeText(this, "Local deletado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao deletar local!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLocais();
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
            List<Local> lista = repo.findAll();
            runOnUiThread(() -> {
                if (lista != null && !lista.isEmpty()) {
                    adapter.updateLocais(lista);
                    repo.setLocais(lista);
                    atualizarContador();
                } else {
                    adapter.updateLocais(repo.getLocais());
                    atualizarContador();
                }
            });
        }).start();
    }
}