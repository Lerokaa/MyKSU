package com.example.myksu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.CameraUpdateFactory;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private MarkerManager markerManager;
    private RouteManager routeManager;
    private DialogManager dialogManager;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        progressManager = ProgressManager.getInstance();
        progressManager.loadProgress(this);

        // Инициализация компонентов
        locationManager = new LocationManager(this);
        markerManager = new MarkerManager(this);
        routeManager = new RouteManager(this);
        dialogManager = new DialogManager(this);

        // Инициализация карты
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Настройка кнопок
        setupButtons();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        try {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            // Инициализация менеджеров с картой
            locationManager.initWithMap(mMap);
            markerManager.initWithMap(mMap);
            routeManager.initWithMap(mMap);

            // Добавление маркеров
            markerManager.addBuildingMarkers();
            markerManager.addDormitoryMarkers();

            if (!markerManager.hasBuildingMarkers()) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(57.759625, 40.942470), 14));
            }

            locationManager.enableMyLocation();
            locationManager.startLocationUpdates();

        } catch (Exception e) {
            Log.e("MapActivity", "Error in onMapReady: " + e.getMessage());
            Toast.makeText(this, "Ошибка инициализации карты", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return markerManager.handleMarkerClick(marker);
    }

    private void setupButtons() {
        ImageButton locationButton = findViewById(R.id.my_location_button);
        locationButton.setOnClickListener(v -> locationManager.centerOnUserLocation());

        ImageButton navigationButton = findViewById(R.id.navigation_button);
        navigationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> dialogManager.showSettingsDialog());
    }

    // Геттеры для менеджеров
    public GoogleMap getMap() { return mMap; }
    public ProgressManager getProgressManager() { return progressManager; }
    public RouteManager getRouteManager() { return routeManager; }
    public DialogManager getDialogManager() { return dialogManager; }
    public LocationManager getLocationManager() { return locationManager; }
}