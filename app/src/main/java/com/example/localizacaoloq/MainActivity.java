package com.example.localizacaoloq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.utils.NavBarHelper;

public class MainActivity extends AppCompatActivity {
    private Button btnLogar;
    private Button btnCriar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCriar=findViewById(R.id.btnConta);
        btnLogar=findViewById(R.id.btnLoginSistema);

        btnCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginSistema();
            }
        });
        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCriarConta();
            }
        });
    }
    private void startCriarConta(){
        Intent navegar=new Intent(this, FormHome.class);
        startActivity((navegar));
    }
    private void startLoginSistema(){

        Intent navegar=new Intent(this, CriarConta.class);
        startActivity((navegar));
    }
}