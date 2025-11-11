package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.adapter.AdapterLocal;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.LocalRepository;
import com.example.localizacaoloq.utils.NavBarHelper;

import java.util.List;

public class LocalForm extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdapterLocal adapter;
    private TextView tvContador;
    private ImageButton btnAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_local_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NavBarHelper.setup(this);

        recyclerView = findViewById(R.id.recyclerViewLocais);
        adapter = new AdapterLocal();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tvContador = findViewById(R.id.tvContador);
        atualizarContador();
        btnAdd=findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistarLocal();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        List<Local> locaisAtualizados = LocalRepository.getInstance().getLocais();
        adapter.updateLocais(locaisAtualizados);
        atualizarContador();
    }
    private void atualizarContador() {
        int count = LocalRepository.getInstance().getLocais().size();
        String texto = String.format("%d local(is) disponível(is)", count);
        tvContador.setText(texto);
    }
    private void startRegistarLocal(){
        Intent intent=new Intent(this, FormLocalRegistado.class);
        startActivity(intent);
    }

}