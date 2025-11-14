package com.example.localizacaoloq.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.localizacaoloq.R;

import com.example.localizacaoloq.model.Local;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.example.localizacaoloq.activities.FullscreenMap;
import com.example.localizacaoloq.activities.FormHome;

public class FormLocalRegistado extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int MAP_REQUEST_CODE = 100;

    private EditText etLocalName, etLatitude, etLongitude, etRadius, etSearchAddress;
    private MaterialRadioButton radioGps, radioWifi;
    private LinearLayout optionGps, optionWifi;
    private CardView cardGpsConfig, cardMap;
    private Button btnCreate, btnUpdateMap, btnSearchAddress, btnMyLocation, btnExpandMap;
    private ImageButton btnBack,btnViewLocals;


    private GoogleMap mMap;
    private Marker currentMarker;
    private Circle currentCircle;
    private boolean isUpdatingFromMap = false;
    private Geocoder geocoder;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<Intent> fullscreenMapLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_local_registado);

        geocoder = new Geocoder(this, Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupListeners();
        initializeMap();
        setupFullscreenMapLauncher();
    }

    private void initializeViews() {
        etLocalName = findViewById(R.id.etLocalName);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        etSearchAddress = findViewById(R.id.etSearchAddress);

        radioGps = findViewById(R.id.radioGps);
        radioWifi = findViewById(R.id.radioWifi);

        optionGps = findViewById(R.id.optionGps);
        optionWifi = findViewById(R.id.optionWifi);

        cardGpsConfig = findViewById(R.id.cardGpsConfig);
        cardMap = findViewById(R.id.cardMap);

        btnCreate = findViewById(R.id.btnCreate);
        btnUpdateMap = findViewById(R.id.btnUpdateMap);
        btnSearchAddress = findViewById(R.id.btnSearchAddress);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnExpandMap = findViewById(R.id.btnExpandMap);
        btnViewLocals = findViewById(R.id.btnViewLocals);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Opção GPS
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

        // Opção WiFi
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

        // Atualizar mapa
        btnUpdateMap.setOnClickListener(v -> updateMapFromInputs());

        // Buscar endereço
        btnSearchAddress.setOnClickListener(v -> searchAddress());

        // Minha localização
        btnMyLocation.setOnClickListener(v -> getMyLocation());

        // Expandir mapa
        btnExpandMap.setOnClickListener(v -> openFullscreenMap());

        // Ver lista de locais
        btnViewLocals.setOnClickListener(v -> openLocalsList());

        // Criar local
        btnCreate.setOnClickListener(v -> createLocal());

        // Listeners de texto
        setupCoordinateListeners();
    }

    private void setupCoordinateListeners() {
        TextWatcher coordinateWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingFromMap && mMap != null) {
                    updateCircleRadius();
                }
            }
        };

        etRadius.addTextChangedListener(coordinateWatcher);
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupFullscreenMapLauncher() {
        fullscreenMapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("latitude", 0);
                        double lng = result.getData().getDoubleExtra("longitude", 0);
                        double rad = result.getData().getDoubleExtra("radius", 500);

                        isUpdatingFromMap = true;
                        etLatitude.setText(String.format(Locale.getDefault(), "%.6f", lat));
                        etLongitude.setText(String.format(Locale.getDefault(), "%.6f", lng));
                        etRadius.setText(String.format(Locale.getDefault(), "%.0f", rad));
                        isUpdatingFromMap = false;

                        updateMapFromInputs();
                        getAddressFromLocation(lat, lng);
                    }
                }
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        updateMapFromInputs();

        mMap.setOnMapClickListener(latLng -> {
            isUpdatingFromMap = true;
            updateMarkerAndInputs(latLng);
            isUpdatingFromMap = false;
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

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

            LatLng position = new LatLng(lat, lng);
            updateMarkerAndCircle(position, radius);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkerAndInputs(LatLng latLng) {
        etLatitude.setText(String.format(Locale.getDefault(), "%.6f", latLng.latitude));
        etLongitude.setText(String.format(Locale.getDefault(), "%.6f", latLng.longitude));

        try {
            double radius = Double.parseDouble(etRadius.getText().toString());
            updateMarkerAndCircle(latLng, radius);
        } catch (NumberFormatException e) {
            updateMarkerAndCircle(latLng, 500);
        }

        getAddressFromLocation(latLng.latitude, latLng.longitude);
    }

    private void updateMarkerAndCircle(LatLng position, double radius) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        if (currentCircle != null) {
            currentCircle.remove();
        }

        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Local Selecionado")
                .draggable(true));

        currentCircle = mMap.addCircle(new CircleOptions()
                .center(position)
                .radius(radius)
                .strokeColor(Color.parseColor("#D32F2F"))
                .strokeWidth(2)
                .fillColor(Color.parseColor("#33D32F2F")));
    }

    private void updateCircleRadius() {
        if (currentCircle != null && currentMarker != null) {
            try {
                double radius = Double.parseDouble(etRadius.getText().toString());
                currentCircle.setRadius(radius);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void searchAddress() {
        String addressText = etSearchAddress.getText().toString().trim();
        if (addressText.isEmpty()) {
            Toast.makeText(this, "Digite um endereço", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocationName(addressText, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                isUpdatingFromMap = true;
                etLatitude.setText(String.format(Locale.getDefault(), "%.6f", address.getLatitude()));
                etLongitude.setText(String.format(Locale.getDefault(), "%.6f", address.getLongitude()));
                isUpdatingFromMap = false;

                updateMarkerAndCircle(location, Double.parseDouble(etRadius.getText().toString()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));

                Toast.makeText(this, "Endereço encontrado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Endereço não encontrado", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao buscar endereço", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void getAddressFromLocation(double lat, double lng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                etSearchAddress.setText(addressText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        isUpdatingFromMap = true;
                        updateMarkerAndInputs(myLocation);
                        isUpdatingFromMap = false;

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
                        Toast.makeText(this, "Localização obtida!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFullscreenMap() {
        try {
            double lat = Double.parseDouble(etLatitude.getText().toString());
            double lng = Double.parseDouble(etLongitude.getText().toString());
            double radius = Double.parseDouble(etRadius.getText().toString());

            Intent intent = new Intent(this, com.example.localizacaoloq.activities.FullscreenMap.class);
            intent.putExtra("latitude", lat);
            intent.putExtra("longitude", lng);
            intent.putExtra("radius", radius);
            fullscreenMapLauncher.launch(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLocalsList() {
        Intent intent = new Intent(this, FormHome.class);
        startActivity(intent);
    }

    private void createLocal() {
        String localName = etLocalName.getText().toString().trim();

        if (localName.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o nome do local", Toast.LENGTH_SHORT).show();
            return;
        }

        if (radioGps.isChecked()) {
            try {
                double lat = Double.parseDouble(etLatitude.getText().toString());
                double lng = Double.parseDouble(etLongitude.getText().toString());
                double radius = Double.parseDouble(etRadius.getText().toString());




            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            }
        } else if (radioWifi.isChecked()) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}