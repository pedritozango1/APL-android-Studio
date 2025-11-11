package com.example.localizacaoloq.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.model.LocalGPS;
import com.example.localizacaoloq.model.LocalRepository;
import com.example.localizacaoloq.model.LocalWifi;

import java.util.Arrays;

public class FormLocalRegistado extends AppCompatActivity {
    private ImageButton btnBack;
    private RadioButton radioGps, radioWifi;
    private CardView cardGpsConfig, cardWifiConfig;
    private EditText etLocalName, etLatitude, etLongitude, etRadius, etWifiIds;
    private Button btnCreate;
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
        inicializarViews();
        configurarEventos();

        toggleConfiguracao(true);
        btnBack=findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void inicializarViews() {
        btnBack = findViewById(R.id.btnBack);
        radioGps = findViewById(R.id.radioGps);
        radioWifi = findViewById(R.id.radioWifi);
        cardGpsConfig = findViewById(R.id.cardGpsConfig);
        cardWifiConfig = findViewById(R.id.cardWifiConfig);
        etLocalName = findViewById(R.id.etLocalName);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        etWifiIds = findViewById(R.id.etWifiIds);
        btnCreate = findViewById(R.id.btnCreate);
    }
    private void configurarEventos() {
        btnBack.setOnClickListener(v -> finish());

        // Listeners para as opções de tipo (radio buttons via LinearLayout clicks para toggle)
        LinearLayout optionGps = findViewById(R.id.optionGps);
        LinearLayout optionWifi = findViewById(R.id.optionWifi);

        optionGps.setOnClickListener(v -> {
            radioGps.setChecked(true);
            radioWifi.setChecked(false);
            toggleConfiguracao(true); // Mostrar GPS
        });

        optionWifi.setOnClickListener(v -> {
            radioWifi.setChecked(true);
            radioGps.setChecked(false);
            toggleConfiguracao(false); // Mostrar WiFi
        });

        // Listener para radio buttons diretos (caso clicados isoladamente)
        radioGps.setOnClickListener(v -> {
            radioGps.setChecked(true);
            radioWifi.setChecked(false);
            toggleConfiguracao(true);
        });

        radioWifi.setOnClickListener(v -> {
            radioWifi.setChecked(true);
            radioGps.setChecked(false);
            toggleConfiguracao(false);
        });

        // Botão criar local
        btnCreate.setOnClickListener(v -> criarLocal());
    }

    private void toggleConfiguracao(boolean isGps) {
        if (isGps) {
            cardGpsConfig.setVisibility(View.VISIBLE);
            cardWifiConfig.setVisibility(View.GONE);
        } else {
            cardGpsConfig.setVisibility(View.GONE);
            cardWifiConfig.setVisibility(View.VISIBLE);
        }
    }
    private void criarLocal() {

        String nome = etLocalName.getText().toString().trim();
        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome do local é obrigatório!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGps = radioGps.isChecked();
        if (isGps) {
            // Validação GPS
            String latStr = etLatitude.getText().toString().trim();
            String lonStr = etLongitude.getText().toString().trim();
            String radiusStr = etRadius.getText().toString().trim();

            if (latStr.isEmpty() || lonStr.isEmpty() || radiusStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos de GPS!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                int radius = Integer.parseInt(radiusStr);

                if (radius <= 0) {
                    Toast.makeText(this, "O raio deve ser maior que 0!", Toast.LENGTH_SHORT).show();
                    return;
                }
                LocalRepository.getInstance().adicionarLocalGPS(new LocalGPS(nome,lat,lon,12,radius));
                // Aqui: Criar local GPS (ex: salvar em repo ou DB)
                Toast.makeText(this, String.format("Local GPS criado: %s [%.4f, %.4f, %dm]", nome, lat, lon, radius), Toast.LENGTH_LONG).show();
                // finish(); // Opcional: Voltar após criação

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Formato inválido nos campos de GPS!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Validação WiFi
            String wifiIdsStr = etWifiIds.getText().toString().trim();
            if (wifiIdsStr.isEmpty()) {
                Toast.makeText(this, "Insira pelo menos um ID de WiFi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] wifiIds = wifiIdsStr.split(",");
            if (wifiIds.length == 0) {
                Toast.makeText(this, "IDs de WiFi inválidos!", Toast.LENGTH_SHORT).show();
                return;
            }
            LocalRepository.getInstance().adicionarLocalWifi(new LocalWifi(nome,Arrays.asList(wifiIds)));
            // Aqui: Criar local WiFi (ex: salvar lista de IDs)
            Toast.makeText(this, String.format("Local WiFi criado: %s [%d IDs]", nome, wifiIds.length), Toast.LENGTH_LONG).show();
            // finish(); // Opcional: Voltar após criação
        }

        // Limpar campos (opcional)
        etLocalName.setText("");
        etLatitude.setText("38.7223");
        etLongitude.setText("-9.1393");
        etRadius.setText("500");
        etWifiIds.setText("");
    }

}