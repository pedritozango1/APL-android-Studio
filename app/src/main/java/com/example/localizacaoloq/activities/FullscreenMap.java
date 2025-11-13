package com.example.localizacaoloq.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.localizacaoloq.R;
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

public class FullscreenMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private Marker currentMarker;
    private Circle currentCircle;
    private FusedLocationProviderClient fusedLocationClient;

    private ImageButton btnBack, btnMyLocation;
    private Button btnConfirm;

    private double latitude = -8.8368;
    private double longitude = 13.2343;
    private double radius = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_map);

        // Receber dados da intent
        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra("latitude", -8.8368);
            longitude = intent.getDoubleExtra("longitude", 13.2343);
            radius = intent.getDoubleExtra("radius", 500);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupListeners();
        initializeMap();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMyLocation.setOnClickListener(v -> getMyLocation());

        btnConfirm.setOnClickListener(v -> {
            if (currentMarker != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", currentMarker.getPosition().latitude);
                resultIntent.putExtra("longitude", currentMarker.getPosition().longitude);
                resultIntent.putExtra("radius", radius);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Por favor, selecione uma localização no mapa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fullscreenMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Adicionar marcador inicial
        LatLng initialPosition = new LatLng(latitude, longitude);
        updateMarkerAndCircle(initialPosition, radius);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15));

        // Listener para cliques no mapa
        mMap.setOnMapClickListener(latLng -> updateMarkerAndCircle(latLng, radius));

        // Listener para arrastar marcador
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {}

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
                if (currentCircle != null) {
                    currentCircle.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                updateMarkerAndCircle(marker.getPosition(), radius);
            }
        });

        // Tentar mostrar localização atual
        enableMyLocation();
    }

    private void updateMarkerAndCircle(LatLng position, double rad) {
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
                .radius(rad)
                .strokeColor(Color.parseColor("#D32F2F"))
                .strokeWidth(2)
                .fillColor(Color.parseColor("#33D32F2F")));
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
                        updateMarkerAndCircle(myLocation, radius);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
                        Toast.makeText(this, "Localização atual obtida", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation();
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}