package com.example.myksu;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final float PROXIMITY_RADIUS = 52;
    private static final String DIRECTIONS_API_KEY = "5b3ce3597851110001cf624884b1501b04444c8f9d22b4c100ef261c";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentUserLocation;
    private final Map<Marker, Boolean> buildingMarkers = new HashMap<>();
    private final Map<Marker, Boolean> dormitoryMarkers = new HashMap<>();
    private final Map<Marker, Boolean> clickedMarkers = new HashMap<>();
    private Marker currentSelectedMarker;
    private Polyline currentRoute;
    private final Map<Marker, Integer> buildingIds = new HashMap<>();
    private final OkHttpClient httpClient = new OkHttpClient();
    ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MusicManager.stopMusic();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        progressManager = ProgressManager.getInstance();
        progressManager.loadProgress(this);

        // Инициализация карты
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageButton locationButton = findViewById(R.id.my_location_button);
        locationButton.setOnClickListener(v -> {
            if (currentUserLocation != null && mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 16));
            } else {
                Toast.makeText(this, "Местоположение не получено", Toast.LENGTH_SHORT).show();
                requestLastKnownLocation();
            }
        });

        ImageButton navigationButton = findViewById(R.id.navigation_button);
        navigationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> showSettingsDialog());
    }

    private void requestLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 16));
                        }
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        try {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            addBuildingMarkers();
            addDormitoryMarkers();
            updateMarkersBasedOnProgress(); // Добавляем вызов здесь

            if (!buildingMarkers.isEmpty()) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(57.759625, 40.942470), 14));
            }

            enableMyLocation();
            startLocationUpdates();

        } catch (Exception e) {
            Log.e("MapActivity", "Error in onMapReady: " + e.getMessage());
            Toast.makeText(this, "Ошибка инициализации карты", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkersBasedOnProgress() {
        for (Map.Entry<Marker, Integer> entry : buildingIds.entrySet()) {
            Marker marker = entry.getKey();
            int buildingId = entry.getValue();

            if (progressManager.isWasBuildingDialog(buildingId)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker));
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_non_marker));
            }
        }
    }

    private void addBuildingMarkers() {
        List<LatLng> buildingLocations = Arrays.asList(
                new LatLng(57.759625, 40.942470),
                new LatLng(57.766919, 40.918577),
                new LatLng(57.761681, 40.940083),
                new LatLng(57.768314, 40.915687),
                new LatLng(57.760810, 40.940021),
                new LatLng(57.767802, 40.917167),
                new LatLng(57.767411, 40.917096),
                new LatLng(57.760810, 40.940021),
                new LatLng(57.736841, 40.920328),
                new LatLng(57.778410, 40.913353),
                new LatLng(57.800863, 41.003536)
        );

        List<String> buildingTitles = Arrays.asList(
                "Главный корпус",
                "А1 корпус",
                "Б корпус",
                "Б1 корпус",
                "В корпус",
                "В1 корпус",
                "Г1 корпус",
                "Д корпус",
                "Е корпус",
                "Спортивный корпус",
                "ИПП корпус"
        );

        for (int i = 0; i < buildingLocations.size(); i++) {
            try {
                int buildingId = i + 1;
                int iconRes;

                if (progressManager.isWasBuildingDialog(buildingId)) {
                    // Для зданий с пройденным диалогом - иконка btn_icons_marker
                    iconRes = R.drawable.btn_icons_marker;
                } else {
                    // Для остальных - обычная иконка
                    iconRes = R.drawable.btn_icons_non_marker;
                }

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(buildingLocations.get(i))
                        .title(buildingTitles.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(iconRes))
                );

                if (marker != null) {
                    buildingMarkers.put(marker, false);
                    clickedMarkers.put(marker, false);
                    buildingIds.put(marker, buildingId);
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error adding building marker: " + e.getMessage());
            }
        }
    }

    private void addDormitoryMarkers() {
        List<LatLng> dormitoryLocations = Arrays.asList(
                new LatLng(57.754431, 40.952182),
                new LatLng(57.753951, 40.954221),
                new LatLng(57.736553, 40.920300),
                new LatLng(57.755104, 40.955613),
                new LatLng(57.755233, 40.954607),
                new LatLng(57.767923, 40.918962)
        );

        List<String> dormitoryTitles = Arrays.asList(
                "Общежитие №1",
                "Общежитие №2",
                "Общежитие №3",
                "Общежитие №4",
                "Общежитие №5",
                "Общежитие №6"
        );

        for (int i = 0; i < dormitoryLocations.size(); i++) {
            try {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(dormitoryLocations.get(i))
                        .title(dormitoryTitles.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_two))
                );
                if (marker != null) {
                    dormitoryMarkers.put(marker, false);
                    clickedMarkers.put(marker, false);
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error adding dormitory marker: " + e.getMessage());
            }
        }
    }

    private void showBuildingDialog(Marker marker) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
            builder.setView(dialogView);

            TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
            TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
            Button routeButton = dialogView.findViewById(R.id.route_button);
            ImageButton closeButton = dialogView.findViewById(R.id.closeButton);

            dialogTitle.setText("Корпус недоступен");
            dialogMessage.setText("Подойдите к корпусу, чтобы получить больше информации о нем");

            routeButton.setVisibility(View.VISIBLE);
            routeButton.setOnClickListener(v -> {
                try {
                    buildRouteToBuilding(marker);
                    ((AlertDialog) v.getTag()).dismiss();
                } catch (Exception e) {
                    Log.e("MapActivity", "Error building route: " + e.getMessage());
                    Toast.makeText(MapActivity.this, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show();
                }
            });
            routeButton.setTag(builder.create());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            closeButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Log.e("MapActivity", "Error showing dialog: " + e.getMessage());
            Toast.makeText(this, "Ошибка отображения диалога", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildRouteToBuilding(Marker destinationMarker) {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show();
            requestLastKnownLocation();
            return;
        }

        if (destinationMarker == null || !destinationMarker.isVisible()) {
            Toast.makeText(this, "Неверный маркер назначения", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRoute != null) {
            currentRoute.remove();
        }

        String url = "https://api.openrouteservice.org/v2/directions/foot-walking?" +
                "api_key=" + DIRECTIONS_API_KEY +
                "&start=" + currentUserLocation.longitude + "," + currentUserLocation.latitude +
                "&end=" + destinationMarker.getPosition().longitude + "," + destinationMarker.getPosition().latitude;

        Log.d("RouteRequest", "OpenRouteService URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json, application/geo+json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this, "Ошибка подключения: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("RouteError", "Connection failed", e);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseData = response.body().string();
                    Log.d("RouteResponse", responseData);

                    JSONObject json = new JSONObject(responseData);
                    JSONObject route = json.getJSONArray("features").getJSONObject(0);
                    String points = route.getJSONObject("geometry").getString("coordinates");

                    List<LatLng> path = new ArrayList<>();
                    JSONArray coords = new JSONArray(points);
                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray coord = coords.getJSONArray(i);
                        path.add(new LatLng(coord.getDouble(1), coord.getDouble(0)));
                    }

                    if (path.isEmpty()) {
                        runOnUiThread(() -> {
                            Toast.makeText(MapActivity.this, "Не удалось декодировать путь", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    runOnUiThread(() -> {
                        try {
                            PolylineOptions options = new PolylineOptions()
                                    .addAll(path)
                                    .width(12)
                                    .color(Color.parseColor("#3F51B5"))
                                    .geodesic(true);

                            currentRoute = mMap.addPolyline(options);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(currentUserLocation);
                            builder.include(destinationMarker.getPosition());
                            LatLngBounds bounds = builder.build();

                            try {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                            } catch (Exception e) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationMarker.getPosition(), 15));
                            }
                        } catch (Exception e) {
                            Log.e("MapActivity", "Error drawing route: " + e.getMessage());
                            Toast.makeText(MapActivity.this, "Ошибка отрисовки маршрута", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this, "Ошибка обработки маршрута: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("RouteError", "Processing error", e);
                    });
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        try {
            boolean isCurrentlySelected = marker.equals(currentSelectedMarker);

            if (currentSelectedMarker != null && !currentSelectedMarker.equals(marker)) {
                resetMarkerIcon(currentSelectedMarker);
            }

            if (isCurrentlySelected) {
                resetMarkerIcon(marker);
                currentSelectedMarker = null;
                return true;
            }

            currentSelectedMarker = marker;

            if (buildingMarkers.containsKey(marker)) {
                int buildingId = buildingIds.get(marker);
                boolean wasDialogCompleted = progressManager.isWasBuildingDialog(buildingId);

                // Для зданий с пройденным диалогом - всегда доступны
                if (wasDialogCompleted) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));
                    Intent intent = new Intent(MapActivity.this, InformationAboutKorpus.class);
                    intent.putExtra("BUILDING_ID", buildingId);
                    startActivity(intent);
                }
                // Для остальных - проверяем доступность
                else if (Boolean.TRUE.equals(buildingMarkers.get(marker))) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));
                    Intent intent = new Intent(MapActivity.this, CharactersDialogActivity.class);
                    intent.putExtra("DIALOG_ID", buildingId);
                    startActivity(intent);
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));
                    showBuildingDialog(marker);
                }
            } else if (dormitoryMarkers.containsKey(marker)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked_two));
                String title = marker.getTitle();
                int selectedDormitoryId = Integer.parseInt(title.replaceAll("[^0-9]", ""));
                CustomDialogObshaga dialog = CustomDialogObshaga.newInstance(selectedDormitoryId);
                dialog.show(getSupportFragmentManager(), "dormitory_dialog");
            }

            marker.showInfoWindow();
            return true;
        } catch (Exception e) {
            Log.e("MapActivity", "Error in onMarkerClick: " + e.getMessage());
            return false;
        }
    }

    private int getBuildingNumber(String buildingTitle) {
        switch (buildingTitle) {
            case "Главный корпус": return 1;
            case "Е корпус": return 9;
            case "Б корпус": return 3;
            case "В корпус":
            case "Д корпус": return 5;
            case "А1 корпус": return 2;
            case "Г1 корпус": return 7;
            case "В1 корпус": return 6;
            case "Б1 корпус": return 4;
            case "Спортивный корпус": return 10;
            case "ИПП корпус": return 11;
            default: return -1;
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    Log.e("MapActivity", "SecurityException in enableMyLocation: " + e.getMessage());
                }
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        checkProximityToBuildings();
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("MapActivity", "SecurityException in startLocationUpdates: " + e.getMessage());
        } catch (Exception e) {
            Log.e("MapActivity", "Error starting location updates: " + e.getMessage());
        }
    }

    private void checkProximityToBuildings() {
        if (currentUserLocation == null || mMap == null) {
            return;
        }

        for (Map.Entry<Marker, Boolean> entry : buildingMarkers.entrySet()) {
            Marker marker = entry.getKey();
            int buildingId = buildingIds.get(marker);

            // Пропускаем маркеры с пройденным диалогом
            if (progressManager.isWasBuildingDialog(buildingId)) {
                continue;
            }

            if (marker.equals(currentSelectedMarker)) {
                continue;
            }

            try {
                float[] results = new float[1];
                Location.distanceBetween(
                        currentUserLocation.latitude,
                        currentUserLocation.longitude,
                        marker.getPosition().latitude,
                        marker.getPosition().longitude,
                        results
                );

                if (results[0] <= PROXIMITY_RADIUS) {
                    if (!entry.getValue()) {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_selected));
                        buildingMarkers.put(marker, true);
                    }
                } else {
                    if (entry.getValue()) {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_non_marker));
                        buildingMarkers.put(marker, false);
                    }
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error checking proximity: " + e.getMessage());
            }
        }
    }

    private void resetMarkerIcon(@Nullable Marker marker) {
        if (marker == null) {
            return;
        }

        try {
            if (buildingMarkers.containsKey(marker)) {
                int buildingId = buildingIds.get(marker);

                if (progressManager.isWasBuildingDialog(buildingId)) {
                    // Для зданий с пройденным диалогом - всегда иконка btn_icons_marker
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker));
                } else if (Boolean.TRUE.equals(buildingMarkers.get(marker))) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_selected));
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_non_marker));
                }
            } else if (dormitoryMarkers.containsKey(marker)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_two));
            }
        } catch (Exception e) {
            Log.e("MapActivity", "Error resetting marker icon: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Разрешение на местоположение отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSettingsDialog() {
        try {
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.setContentView(R.layout.dialog_settings);

            settingsDialog.setTitle(null);
            settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Window window = settingsDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(window.getAttributes());
                lp.width = (int) (315 * getResources().getDisplayMetrics().density);
                lp.height = (int) (210 * getResources().getDisplayMetrics().density);
                lp.dimAmount = 0.7f;
                window.setAttributes(lp);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }

            ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(v -> settingsDialog.dismiss());

            SeekBar volumeSeekBar = settingsDialog.findViewById(R.id.volumeSeekBar);
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volumeSeekBar.setMax(maxVolume);
            volumeSeekBar.setProgress(currentVolume);

            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            ImageButton exitButton = settingsDialog.findViewById(R.id.exitButton);
            exitButton.setOnClickListener(v -> {
                settingsDialog.dismiss();
                finishAffinity();
                System.exit(0);
            });

            settingsDialog.show();
        } catch (Exception e) {
            Log.e("MapActivity", "Error showing settings dialog: " + e.getMessage());
            Toast.makeText(this, "Ошибка отображения настроек", Toast.LENGTH_SHORT).show();
        }
    }
}