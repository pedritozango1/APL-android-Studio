package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.utils.NavBarHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FormHome extends AppCompatActivity {
    private ImageView nav_locais_icon;
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
        NavBarHelper.setup(this);
        nav_locais_icon=findViewById(R.id.nav_locais_icon);
        nav_locais_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFOrmLocais();
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab_add);

        if (fab != null) {
            fab.setOnClickListener(v -> {
                startAnuncio();
            });
        } else {
            Log.e("FormHome", "FAB não encontrado!");
        }
    }
    private  void startFOrmLocais(){
        Intent formLocal=new Intent(this, LocalForm.class);
        startActivity(formLocal);
    }
    private void startAnuncio(){
        Intent init=new Intent(this, FormAnuncio.class);
        startActivity(init);
    }
}