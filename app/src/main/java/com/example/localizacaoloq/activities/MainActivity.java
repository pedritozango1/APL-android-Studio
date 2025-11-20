package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AuthRepository;
import com.example.localizacaoloq.model.Auth;
import com.example.localizacaoloq.model.Session;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.model.User;
import com.example.localizacaoloq.Repository.UserRepository;

public class MainActivity extends AppCompatActivity {
    private Button btnLogar;
    private Button btnCriar;
    private Auth auth;
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
        SessionManager sessionManager=new SessionManager(getApplicationContext());
        String id=sessionManager.getSessionId();
        Toast.makeText(this,"",Toast.LENGTH_SHORT).show();
       if(!id.isEmpty()){
            new Thread(() -> {
                AuthRepository authrep = new AuthRepository();
                Session session = authrep.pegarIdSessao(id);
                if (session != null && session.isActive()) {
                    runOnUiThread(() -> startHome());
                }
            }).start();
        }
        btnCriar=findViewById(R.id.btnConta);
        btnLogar=findViewById(R.id.btnLoginSistema);

        btnCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCriarConta();
            }
        });
        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etUsername = findViewById(R.id.etUsername);
                EditText etPassword = findViewById(R.id.etPassword);

                String nome = etUsername.getText().toString().trim();
                String senha = etPassword.getText().toString();
                if (nome.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                User utilizador = new User(nome, senha);
                AuthRepository authRepository = new AuthRepository();
                new Thread(() -> {
                    Session auth = authRepository.login(utilizador);
                    runOnUiThread(() -> {

                        if (auth != null && auth.getSessionId() != null) {
                            // Salvar sessionId localmente
                            SessionManager sessionManager = new SessionManager(MainActivity.this);
                            sessionManager.saveSession(auth.getSessionId());
                            Toast.makeText(MainActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                            // Ir para HomeActivity
                            startHome();
                        } else {
                            Toast.makeText(MainActivity.this, "Credenciais inválidas!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });
    }

    private void startCriarConta(){
        Intent navegar=new Intent(this, CriarConta.class);
        startActivity((navegar));
    }
    private void startHome(){
        Intent intent = new Intent(MainActivity.this, FormHome.class);
        startActivity(intent);
    }

}