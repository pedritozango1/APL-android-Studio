package com.example.localizacaoloq.activities;



import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.adapter.MeusAnunciosAdapter;
import com.example.localizacaoloq.utils.NavBarHelper;


import java.util.ArrayList;
import java.util.List;

public class MeusAnuncios extends AppCompatActivity {

    RecyclerView recyclerView;
    MeusAnunciosAdapter adapter;
    List<String> lista = new ArrayList<>(); // Depois substituis pelo teu modelo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        NavBarHelper.setup(this);

        recyclerView = findViewById(R.id.recyclerMeusAnuncios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Simulação de dados
        lista.add("Anúncio 1");
        lista.add("Anúncio 2");
        lista.add("Anúncio 3");

        adapter = new MeusAnunciosAdapter(lista);
        recyclerView.setAdapter(adapter);
    }
}
