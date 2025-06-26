package com.example.myksu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ImageButton;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private LatLng currentUserLocation;
    private final Map<Marker, Boolean> buildingMarkers = new HashMap<>();
    private final Map<Marker, Boolean> dormitoryMarkers = new HashMap<>();
    private final Map<Marker, Boolean> clickedMarkers = new HashMap<>();
    private Marker currentSelectedMarker = null;
    private static final float PROXIMITY_RADIUS = 52;

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
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        List<LatLng> buildingLocations = Arrays.asList(
                new LatLng(57.759625, 40.942470),
                new LatLng(57.736841, 40.920328),
                new LatLng(57.761681, 40.940083),
                new LatLng(57.760810, 40.940021),
                new LatLng(57.760810, 40.940021),
                new LatLng(57.766919, 40.918577),
                new LatLng(57.767411, 40.917096),
                new LatLng(57.767802, 40.917167),
                new LatLng(57.768314, 40.915687),
                new LatLng(57.778410, 40.913353),
                new LatLng(57.800863, 41.003536)
        );

        List<String> buildingTitles = Arrays.asList(
                "Главный корпус",
                "Е корпус",
                "Б корпус",
                "В корпус",
                "Д корпус",
                "А1 корпус",
                "Г1 корпус",
                "В1 корпус",
                "Б1 корпус",
                "Спортивный корпус",
                "ИПП корпус"
        );

        for (int i = 0; i < buildingLocations.size(); i++) {
            int iconRes = (i == 0) ? R.drawable.marker : R.drawable.non_marker;

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(buildingLocations.get(i))
                    .title(buildingTitles.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(iconRes))
            );
            if (marker != null) {
                buildingMarkers.put(marker, false);
                clickedMarkers.put(marker, false);
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
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_two))
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

    private void showBuildingDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
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
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    checkProximityToBuildings();
                }
            }
        }, Looper.getMainLooper());
    }

    private void checkProximityToBuildings() {
        if (currentUserLocation == null || mMap == null) return;

        for (Map.Entry<Marker, Boolean> entry : buildingMarkers.entrySet()) {
            Marker marker = entry.getKey();

            if (marker.equals(currentSelectedMarker)) continue;

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
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_selected));
                    buildingMarkers.put(marker, true);
                }
            } else {
                if (entry.getValue()) {
                    int iconRes = marker.getTitle().equals("Главный корпус")
                            ? R.drawable.marker
                            : R.drawable.non_marker;
                    marker.setIcon(BitmapDescriptorFactory.fromResource(iconRes));
                    buildingMarkers.put(marker, false);
                }
            }
        }
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
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_clicked));

                if (marker.getTitle().equals("ИПП корпус")) {
                    showBuildingDialog("Корпус недоступен",
                            "Чтобы активировать данный корпус, нужно пройти все корпуса");
                } else if (!marker.getTitle().equals("Главный корпус")) {
                    showBuildingDialog("Корпус недоступен",
                            "Чтобы активировать данный корпус, нужно для начала пройти Главный корпус");
                }

            } else if (dormitoryMarkers.containsKey(marker)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_clicked_two));
                // Больше не показываем всплывающие окна для общежитий
            }

            currentSelectedMarker = marker;
        }

        marker.showInfoWindow();
        return true;
    }

    private void resetMarkerIcon(@Nullable Marker marker) {
        if (marker == null) return;

        if (buildingMarkers.containsKey(marker)) {
            int iconRes = Boolean.TRUE.equals(buildingMarkers.get(marker))
                    ? R.drawable.marker_selected
                    : (marker.getTitle().equals("Главный корпус")
                    ? R.drawable.marker
                    : R.drawable.non_marker);
            marker.setIcon(BitmapDescriptorFactory.fromResource(iconRes));
        } else if (dormitoryMarkers.containsKey(marker)) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_two));
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
}
