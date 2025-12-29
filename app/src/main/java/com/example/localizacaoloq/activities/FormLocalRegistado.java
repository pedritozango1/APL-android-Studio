package com.example.localizacaoloq.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.LocalRepository;
import com.example.localizacaoloq.model.LocalGPS;

import java.util.Locale;

public class FormLocalRegistado extends AppCompatActivity {

    // Views
    private EditText etLocalName;
    private RadioButton radioGps, radioWifi;
    private EditText etLatitude, etLongitude, etRadius;
    private Button btnMyLocation, btnCreate;
    private ImageButton btnBack;

    // Repositório
    private LocalRepository localRepo;

    // Localização
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_local_registado);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repositório
        localRepo = LocalRepository.getInstance();

        // Inicializar views
        inicializarViews();

        // Configurar listeners
        configurarListeners();

        // Solicitar permissões de localização
        solicitarPermissoesLocalizacao();

        // Tentar obter localização automaticamente
        obterLocalizacaoAutomatica();
    }

    private void inicializarViews() {
        etLocalName = findViewById(R.id.etLocalName);
        radioGps = findViewById(R.id.radioGps);
        radioWifi = findViewById(R.id.radioWifi);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnCreate = findViewById(R.id.btnCreate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void configurarListeners() {
        // Botão voltar
        btnBack.setOnClickListener(v -> finish());

        // Botão minha localização
        btnMyLocation.setOnClickListener(v -> obterLocalizacaoAtual());

        // Botão criar local
        btnCreate.setOnClickListener(v -> criarLocal());

        // Listeners para tipo de local
        findViewById(R.id.optionGps).setOnClickListener(v -> {
            radioGps.setChecked(true);
            radioWifi.setChecked(false);
        });

        findViewById(R.id.optionWifi).setOnClickListener(v -> {
            radioWifi.setChecked(true);
            radioGps.setChecked(false);
            Toast.makeText(this, "Para WiFi, adicione as redes manualmente", Toast.LENGTH_SHORT).show();
        });
    }

    private void solicitarPermissoesLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            Log.d("FormLocal", "Permissão de localização já concedida");
        }
    }

    private void obterLocalizacaoAutomatica() {
        // Tentar obter localização automaticamente ao abrir o formulário
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Pequena pausa para UI carregar
                runOnUiThread(this::obterLocalizacaoAtual);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void obterLocalizacaoAtual() {
        // Verificar permissões
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this,
                    "Permissão de localização necessária. Conceda permissão nas configurações.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnMyLocation.setEnabled(false);
        btnMyLocation.setText("Obtendo localização...");

        new Thread(() -> {
            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                // Tentar obter localização
                Location location = null;

                // Tentar GPS primeiro
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    try {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            Log.d("FormLocal", "Localização obtida do GPS");
                        }
                    } catch (SecurityException e) {
                        Log.e("FormLocal", "Erro de permissão ao acessar GPS: " + e.getMessage());
                    }
                }

                // Se GPS não estiver disponível, tentar rede
                if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            Log.d("FormLocal", "Localização obtida da Rede");
                        }
                    } catch (SecurityException e) {
                        Log.e("FormLocal", "Erro de permissão ao acessar Rede: " + e.getMessage());
                    }
                }

                // Se ainda não tiver, tentar localização passiva
                if (location == null) {
                    try {
                        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        if (location != null) {
                            Log.d("FormLocal", "Localização obtida do Passive Provider");
                        }
                    } catch (SecurityException e) {
                        Log.e("FormLocal", "Erro de permissão ao acessar Passive Provider: " + e.getMessage());
                    }
                }

                final Location finalLocation = location;

                runOnUiThread(() -> {
                    if (finalLocation != null) {
                        // Preencher os campos com a localização obtida
                        etLatitude.setText(String.format(Locale.US, "%.6f", finalLocation.getLatitude()));
                        etLongitude.setText(String.format(Locale.US, "%.6f", finalLocation.getLongitude()));
                        etRadius.setText("100"); // Raio padrão de 100 metros

                        String mensagem = String.format(
                                "📍 Localização obtida:\nLat: %.6f\nLon: %.6f\nPrecisão: %.1fm",
                                finalLocation.getLatitude(),
                                finalLocation.getLongitude(),
                                finalLocation.getAccuracy()
                        );

                        Toast.makeText(FormLocalRegistado.this, mensagem, Toast.LENGTH_LONG).show();
                        Log.d("FormLocal", mensagem);
                    } else {
                        Toast.makeText(FormLocalRegistado.this,
                                "📍 Localização não disponível.\nVerifique se o GPS está ligado.",
                                Toast.LENGTH_LONG).show();
                    }

                    btnMyLocation.setEnabled(true);
                    btnMyLocation.setText("Minha Localização Atual");
                });

            } catch (Exception e) {
                Log.e("FormLocal", "Erro ao obter localização: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(FormLocalRegistado.this,
                            "Erro ao obter localização: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnMyLocation.setEnabled(true);
                    btnMyLocation.setText("Minha Localização Atual");
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permissão de localização concedida!", Toast.LENGTH_SHORT).show();
                // Tentar obter localização após permissão concedida
                obterLocalizacaoAtual();
            } else {
                Toast.makeText(this,
                        "❌ Permissão de localização negada.\nVocê precisa permitir localização para criar locais.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void criarLocal() {
        if (!validarFormulario()) {
            return;
        }

        String nome = etLocalName.getText().toString().trim();

        if (radioGps.isChecked()) {
            criarLocalGPS(nome);
        } else if (radioWifi.isChecked()) {
            Toast.makeText(this, "Funcionalidade WiFi em desenvolvimento", Toast.LENGTH_SHORT).show();
        }
    }

    private void criarLocalGPS(String nome) {
        try {
            double latitude = Double.parseDouble(etLatitude.getText().toString());
            double longitude = Double.parseDouble(etLongitude.getText().toString());
            double raio = Double.parseDouble(etRadius.getText().toString());

            LocalGPS localGPS = new LocalGPS(nome, latitude, longitude, raio);

            btnCreate.setEnabled(false);
            btnCreate.setText("Criando...");

            new Thread(() -> {
                LocalGPS localCriado = (LocalGPS) localRepo.create(localGPS);

                runOnUiThread(() -> {
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Criar Local");

                    if (localCriado != null) {
                        Toast.makeText(FormLocalRegistado.this,
                                "Local criado com sucesso!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(FormLocalRegistado.this,
                                "Erro ao criar local. Tente novamente.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Erro: coordenadas inválidas. Obtenha a localização novamente.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarFormulario() {
        // Validar nome
        if (TextUtils.isEmpty(etLocalName.getText())) {
            etLocalName.setError("Nome do local é obrigatório");
            etLocalName.requestFocus();
            return false;
        }

        if (etLocalName.getText().toString().trim().length() > 100) {
            etLocalName.setError("Nome muito longo (máx. 100 caracteres)");
            etLocalName.requestFocus();
            return false;
        }

        // Validar tipo de local
        if (!radioGps.isChecked() && !radioWifi.isChecked()) {
            Toast.makeText(this, "Selecione o tipo de local", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Se for GPS, validar coordenadas
        if (radioGps.isChecked()) {
            if (TextUtils.isEmpty(etLatitude.getText())) {
                Toast.makeText(this, "É necessário obter a localização atual", Toast.LENGTH_SHORT).show();
                btnMyLocation.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(etLongitude.getText())) {
                Toast.makeText(this, "É necessário obter a localização atual", Toast.LENGTH_SHORT).show();
                btnMyLocation.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(etRadius.getText())) {
                etRadius.setError("Raio é obrigatório");
                etRadius.requestFocus();
                return false;
            }

            try {
                double raio = Double.parseDouble(etRadius.getText().toString());
                if (raio <= 0) {
                    etRadius.setError("Raio deve ser maior que zero");
                    etRadius.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                etRadius.setError("Raio inválido");
                etRadius.requestFocus();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar recursos se necessário
    }
}