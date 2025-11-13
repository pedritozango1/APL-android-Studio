package com.example.localizacaoloq.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.localizacaoloq.Repository.UserRepository;

public class CriarConta extends AppCompatActivity {
    private UserRepository repository;
    private User utilizador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_criar_conta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        configurarEventos();
    }

    private void configurarEventos() {
        Button btnBack = findViewById(R.id.btnArrowBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnCreate = findViewById(R.id.btnCreateAccount);
        btnCreate.setOnClickListener(v -> criarUsuario());

        // Botão fazer login (opcional, para navegar para tela de login)
        Button btnLogin = findViewById(R.id.btnLoginLink);
        btnLogin.setOnClickListener(v -> {
            // Aqui podes navegar para a activity de login, ex: startActivity(new Intent(this, LoginActivity.class));
            Toast.makeText(this, "Navegar para login", Toast.LENGTH_SHORT).show();
        });
    }

    private void criarUsuario() {
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            repository = new UserRepository();
            utilizador = new User(username, password);

            new Thread(() -> {
                // CHAVE: chamar registerUser apenas uma vez e atribuir o resultado
                String result = repository.registerUser(utilizador);
                runOnUiThread(() ->
                        Toast.makeText(CriarConta.this, result, Toast.LENGTH_LONG).show()
                );
            }).start();

            // Opcional: Auto-login após criação
            // String sessionId = auth.login(username, password);
            // if (sessionId != null) {
            //     // Navegar para tela principal com sessão
            // }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao criar conta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
