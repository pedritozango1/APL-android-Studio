package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AuthRepository;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;

public class configuracao extends AppCompatActivity {
    private LinearLayout buttonCardLogout;
    private String idSessa;
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
        buttonCardLogout=findViewById(R.id.card_logout);
        SessionManager sessionManager=new SessionManager(getApplicationContext());
        idSessa=sessionManager.getSessionId();
        Toast.makeText(this,"",Toast.LENGTH_SHORT).show();
        buttonCardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminarSessao();
            }
        });

    }
    private void terminarSessao(){
        if(!idSessa.isEmpty()){
            new Thread(() -> {
                AuthRepository authrep = new AuthRepository();
                String session = authrep.logout(idSessa);
                if (session!=null) {
                    runOnUiThread(() -> {
                        finishAffinity(); // fecha todas as activities da pilha
                        startActivity(new Intent(this, MainActivity.class));
                    });
                }
            }).start();
        }
    }
}