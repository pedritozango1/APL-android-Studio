package com.example.localizacaoloq.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.LocalRepository;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.LocalGPS;
import com.example.localizacaoloq.model.LocalWifi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormLocalRegistado extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private EditText etLocalName, etLatitude, etLongitude, etRadius;
    private MaterialRadioButton radioGps, radioWifi;
    private LinearLayout optionGps, optionWifi;
    private CardView cardGpsConfig, cardMap;
    private Button btnCreate, btnMyLocation;
    private ImageButton btnBack;

    private GoogleMap mMap;
    private Marker currentMarker;
    private Circle currentCircle;

    private boolean isUpdatingFromMap = false;

    private Geocoder geocoder;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_local_registado);

        geocoder = new Geocoder(this, Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupListeners();
        initializeMap();
    }

    private void initializeViews() {
        etLocalName = findViewById(R.id.etLocalName);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);

        radioGps = findViewById(R.id.radioGps);
        radioWifi = findViewById(R.id.radioWifi);

        optionGps = findViewById(R.id.optionGps);
        optionWifi = findViewById(R.id.optionWifi);

        cardGpsConfig = findViewById(R.id.cardGpsConfig);
        cardMap = findViewById(R.id.cardMap);

        btnCreate = findViewById(R.id.btnCreate);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // GPS selecionado
        optionGps.setOnClickListener(v -> {
            radioGps.setChecked(true);
            radioWifi.setChecked(false);
            cardGpsConfig.setVisibility(View.VISIBLE);
            cardMap.setVisibility(View.VISIBLE);
        });

        radioGps.setOnClickListener(v -> {
            radioWifi.setChecked(false);
            cardGpsConfig.setVisibility(View.VISIBLE);
            cardMap.setVisibility(View.VISIBLE);
        });

        // WIFI selecionado
        optionWifi.setOnClickListener(v -> {
            radioWifi.setChecked(true);
            radioGps.setChecked(false);
            cardGpsConfig.setVisibility(View.GONE);
            cardMap.setVisibility(View.GONE);
        });

        radioWifi.setOnClickListener(v -> {
            radioGps.setChecked(false);
            cardGpsConfig.setVisibility(View.GONE);
            cardMap.setVisibility(View.GONE);
        });

        btnMyLocation.setOnClickListener(v -> getMyLocation());

        btnCreate.setOnClickListener(v -> createLocal());
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        updateMapFromInputs();

        mMap.setOnMapClickListener(latLng -> {
            isUpdatingFromMap = true;
            updateMarkerAndInputs(latLng);
            isUpdatingFromMap = false;
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {
                if (currentCircle != null) {
                    currentCircle.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                isUpdatingFromMap = true;
                updateMarkerAndInputs(marker.getPosition());
                isUpdatingFromMap = false;
            }
        });
    }

    private void updateMapFromInputs() {
        try {
            double lat = Double.parseDouble(etLatitude.getText().toString());
            double lng = Double.parseDouble(etLongitude.getText().toString());
            double radius = Double.parseDouble(etRadius.getText().toString());

            LatLng pos = new LatLng(lat, lng);

            updateMarkerAndCircle(pos, radius);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        } catch (Exception e) {
            Toast.makeText(this, "Coordenadas inválidas!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkerAndInputs(LatLng pos) {
        etLatitude.setText(String.format(Locale.getDefault(), "%.6f", pos.latitude));
        etLongitude.setText(String.format(Locale.getDefault(), "%.6f", pos.longitude));

        double radius = 500;
        try {
            radius = Double.parseDouble(etRadius.getText().toString());
        } catch (Exception ignored) {}

        updateMarkerAndCircle(pos, radius);
    }

    private void updateMarkerAndCircle(LatLng pos, double radius) {
        if (mMap == null) return;

        if (currentMarker != null) currentMarker.remove();
        if (currentCircle != null) currentCircle.remove();

        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Local selecionado")
                .draggable(true));

        currentCircle = mMap.addCircle(new CircleOptions()
                .center(pos)
                .radius(radius)
                .strokeColor(Color.RED)
                .strokeWidth(2)
                .fillColor(Color.parseColor("#33D32F2F")));
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        LatLng me = new LatLng(location.getLatitude(), location.getLongitude());

                        isUpdatingFromMap = true;
                        updateMarkerAndInputs(me);
                        isUpdatingFromMap = false;

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 17));
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createLocal() {
        String name = etLocalName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Insira o nome do local!", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalRepository apiRepo = LocalRepository.getInstance();

        if (radioGps.isChecked()) {
            try {
                double lat = Double.parseDouble(etLatitude.getText().toString());
                double lng = Double.parseDouble(etLongitude.getText().toString());
                double raio = Double.parseDouble(etRadius.getText().toString());

                LocalGPS gps = new LocalGPS(name, lat, lng, raio);

                new Thread(() -> {
                    try {
                        Local result = apiRepo.create(gps);

                        runOnUiThread(() -> {
                            if (result != null) {
                                // ✅ Não precisa mais chamar addLocal, o cache já foi atualizado
                                Toast.makeText(this,
                                        "Local GPS criado com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();
                                finish();
                            } else {
                                Toast.makeText(this,
                                        "Erro: resposta nula do servidor!",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(
                                this,
                                "Erro ao comunicar com o servidor: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }).start();

            } catch (Exception e) {
                Toast.makeText(this, "Coordenadas inválidas!", Toast.LENGTH_SHORT).show();
            }

        } else {
            List<String> listaSinais = new ArrayList<>();
            listaSinais.add("SSID_EXEMPLO_1");
            listaSinais.add("SSID_EXEMPLO_2");

            LocalWifi wifi = new LocalWifi(name, listaSinais);

            new Thread(() -> {
                Local result = apiRepo.create(wifi);

                runOnUiThread(() -> {
                    if (result != null) {
                        // ✅ Não precisa mais chamar addLocal, o cache já foi atualizado
                        Toast.makeText(this,
                                "Local WiFi criado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Erro ao enviar para o servidor!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }).start();
        }
    }
}
