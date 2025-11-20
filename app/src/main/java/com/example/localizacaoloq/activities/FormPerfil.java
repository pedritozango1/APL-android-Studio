package com.example.localizacaoloq.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.utils.NavBarHelper;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.adapter.AtributoAdapter;
import com.example.localizacaoloq.model.Atributo;
import com.example.localizacaoloq.utils.NavBarHelper;

import java.util.ArrayList;
import java.util.List;

public class FormPerfil extends AppCompatActivity {

    private EditText inputChave, inputValor;
    private Button btnAdicionar;
    private RecyclerView recycler;
    private AtributoAdapter adapter;
    private List<Atributo> atributos = new ArrayList<>();

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

        NavBarHelper.setup(this);

        inputChave = findViewById(R.id.input_chave);
        inputValor = findViewById(R.id.input_valor);
        btnAdicionar = findViewById(R.id.btn_adicionar_atributo);

        recycler = findViewById(R.id.recyclerAtributos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AtributoAdapter(atributos, position -> {
            atributos.remove(position);
            adapter.notifyItemRemoved(position);
        });

        recycler.setAdapter(adapter);

        btnAdicionar.setOnClickListener(v -> adicionarAtributo());
    }

    private void adicionarAtributo() {
        String key = inputChave.getText().toString().trim();
        String value = inputValor.getText().toString().trim();

        if (key.isEmpty() || value.isEmpty()) return;

        atributos.add(new Atributo(key, value));
        adapter.notifyItemInserted(atributos.size() - 1);

        inputChave.setText("");
        inputValor.setText("");
    }
}
