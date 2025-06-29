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

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final float PROXIMITY_RADIUS = 52;
    private static final String DIRECTIONS_API_KEY = "AIzaSyAOL_tozvB5hMNVQJFFQe8Cz2tFA_pxbOA"; // Замените на ваш API ключ
    private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        List<LatLng> buildingLocations = Arrays.asList(
                new LatLng(57.759625, 40.942470),  // Главный корпус (ID: 1)
                new LatLng(57.766919, 40.918577),  // А1 корпус (ID: 2)
                new LatLng(57.761681, 40.940083),  // Б корпус (ID: 3)
                new LatLng(57.768314, 40.915687),  // Б1 корпус (ID: 4)
                new LatLng(57.760810, 40.940021),  // В корпус (ID: 5)
                new LatLng(57.767802, 40.917167), // В1 корпус (ID: 6)
                new LatLng(57.767411, 40.917096),  // Г1 корпус (ID: 7)
                new LatLng(57.760810, 40.940021),  // Д корпус (ID: 8)
                new LatLng(57.736841, 40.920328), // Е корпус (ID: 9)
                new LatLng(57.778410, 40.913353), // Спортивный корпус (ID: 10)
                new LatLng(57.800863, 41.003536)  // ИПП корпус (ID: 11)
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

        // Добавляем маркеры с ID корпусов (1-11)
        for (int i = 0; i < buildingLocations.size(); i++) {
            int iconRes = (i == 0) ? R.drawable.btn_icons_marker : R.drawable.btn_icons_non_marker;

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(buildingLocations.get(i))
                    .title(buildingTitles.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(iconRes))
            );

            if (marker != null) {
                buildingMarkers.put(marker, false);
                clickedMarkers.put(marker, false);
                buildingIds.put(marker, i + 1); // ID от 1 до 11
            }
        }

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
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(dormitoryLocations.get(i))
                    .title(dormitoryTitles.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_two))
            );
            if (marker != null) {
                dormitoryMarkers.put(marker, false);
                clickedMarkers.put(marker, false);
            }
        }

        if (!buildingLocations.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buildingLocations.get(0), 14));
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        enableMyLocation();
        startLocationUpdates();
    }

    private void showCustomDialog(String title, String message, boolean showRouteButton, Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button routeButton = dialogView.findViewById(R.id.route_button);
        ImageButton closeButton = dialogView.findViewById(R.id.closeButton);

        dialogTitle.setText(title);
        dialogMessage.setText(message);

        if (showRouteButton) {
            routeButton.setVisibility(View.VISIBLE);
            routeButton.setOnClickListener(v -> {
                buildRouteToBuilding(marker);
                ((AlertDialog) v.getTag()).dismiss();
            });
            routeButton.setTag(builder.create());
        } else {
            routeButton.setVisibility(View.GONE);
        }

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void buildRouteToBuilding(Marker destinationMarker) {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRoute != null) {
            currentRoute.remove();
        }

        String origin = currentUserLocation.latitude + "," + currentUserLocation.longitude;
        String destination = destinationMarker.getPosition().latitude + "," + destinationMarker.getPosition().longitude;
        String url = DIRECTIONS_URL + "origin=" + origin + "&destination=" + destination +
                "&mode=walking&key=" + DIRECTIONS_API_KEY;

        Request request = new Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MapActivity.this, "Ошибка при подключении к серверу маршрута", Toast.LENGTH_SHORT).show()
                );
                Log.e("RouteError", "Failed to fetch route", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MapActivity.this, "Сервер вернул ошибку маршрута", Toast.LENGTH_SHORT).show()
                    );
                    Log.e("RouteError", "Unsuccessful response: " + response.code());
                    return;
                }

                try {
                    String responseData = response.body() != null ? response.body().string() : "";
                    JSONObject json = new JSONObject(responseData);

                    if (!json.has("routes") || json.getJSONArray("routes").length() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(MapActivity.this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                        );
                        Log.w("RouteError", "No routes found in response");
                        return;
                    }

                    String points = json.getJSONArray("routes")
                            .getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points");

                    List<LatLng> path = PolyUtil.decode(points);

                    runOnUiThread(() -> {
                        PolylineOptions options = new PolylineOptions()
                                .addAll(path)
                                .width(10)
                                .color(Color.BLUE)
                                .geodesic(true);

                        currentRoute = mMap.addPolyline(options);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationMarker.getPosition(), 15));
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(MapActivity.this, "Ошибка обработки маршрута", Toast.LENGTH_SHORT).show()
                    );
                    Log.e("RouteError", "Exception parsing route", e);
                }
            }
        });
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        boolean isCurrentlySelected = marker.equals(currentSelectedMarker);

        if (currentSelectedMarker != null && !currentSelectedMarker.equals(marker)) {
            resetMarkerIcon(currentSelectedMarker);
        }

        if (isCurrentlySelected) {
            resetMarkerIcon(marker);
            currentSelectedMarker = null;
        } else {
            if (buildingMarkers.containsKey(marker)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));

                int buildingId = buildingIds.get(marker);
                String buildingName = marker.getTitle();

                if ("ИПП корпус".equals(buildingName)) {
                    showCustomDialog("Корпус недоступен",
                            "Чтобы активировать данный корпус, нужно пройти оставшиеся корпуса КГУ",
                            false, marker);
                } else if ("Главный корпус".equals(buildingName)) {
                    showCustomDialog("Корпус недоступен",
                            "Чтобы получить информацию об этом корпусе, подойдите к нему по GPS",
                            true, marker);
                } else {
                    showCustomDialog("Корпус недоступен",
                            "Чтобы активировать данный корпус, нужно для начала пройти Главный корпус",
                            true, marker);
                }
            } else if (dormitoryMarkers.containsKey(marker)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked_two));
                showCustomDialog(marker.getTitle(), "Общежитие", false, marker);
            }

            currentSelectedMarker = marker;
        }

        marker.showInfoWindow();
        return true;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMap != null) {
            mMap.setMyLocationEnabled(true);
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
    }

    private void checkProximityToBuildings() {
        if (currentUserLocation == null || mMap == null) {
            return;
        }

        for (Map.Entry<Marker, Boolean> entry : buildingMarkers.entrySet()) {
            Marker marker = entry.getKey();

            if (marker.equals(currentSelectedMarker)) {
                continue;
            }

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
                    int iconRes = marker.getTitle().equals("Главный корпус")
                            ? R.drawable.btn_icons_marker
                            : R.drawable.btn_icons_non_marker;
                    marker.setIcon(BitmapDescriptorFactory.fromResource(iconRes));
                    buildingMarkers.put(marker, false);
                }
            }
        }
    }

    private void resetMarkerIcon(@Nullable Marker marker) {
        if (marker == null) {
            return;
        }

        if (buildingMarkers.containsKey(marker)) {
            int iconRes = Boolean.TRUE.equals(buildingMarkers.get(marker))
                    ? R.drawable.btn_icons_marker_selected
                    : (marker.getTitle().equals("Главный корпус")
                    ? R.drawable.btn_icons_marker
                    : R.drawable.btn_icons_non_marker);
            marker.setIcon(BitmapDescriptorFactory.fromResource(iconRes));
        } else if (dormitoryMarkers.containsKey(marker)) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_two));
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
    }
}