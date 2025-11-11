package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.localizacaoloq.model.Auth;
import com.example.localizacaoloq.model.SessionManager;
import com.example.localizacaoloq.model.User;
import com.example.localizacaoloq.model.UserRepository;

public class MainActivity extends AppCompatActivity {
    private Button btnLogar;
    private Button btnCriar;
    private UserRepository repository;
    private SessionManager sessionManager;
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
        repository = (UserRepository) getApplicationContext(); // Assumindo que UserRepository é singleton
        sessionManager = new SessionManager();
        auth = new Auth(repository, sessionManager);
        inicializarComp();
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

                // Validações básicas
                if (nome.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return;
                }
                startLogarNosistema(nome, senha);
            }
        });
    }
    private void startLogarNosistema(String nome,String senha){
        String idSession=auth.login(nome,senha);
        if(idSession!=null){
            Intent navegar = new Intent(this, FormHome.class);
            navegar.putExtra("sessionId", idSession);
            startActivity(navegar);
        }else{
            Toast.makeText(this,"Erro usuario não existe no sistema",Toast.LENGTH_SHORT).show();
        }
    }
    private void startCriarConta(){
        Intent navegar=new Intent(this, CriarConta.class);
        startActivity((navegar));
    }
    private  void inicializarComp(){
        UserRepository repository =(UserRepository) getApplicationContext();
        repository.adicionar(new User("alice", "123"));
        repository.adicionar(new User("pedro", "abc"));
    }
}