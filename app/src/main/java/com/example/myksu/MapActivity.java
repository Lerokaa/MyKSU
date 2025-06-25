package com.example.myksu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_PERMISSION_REQUEST = 1;
    private LatLng currentUserLocation;

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

        List<LatLng> locations = Arrays.asList(
                new LatLng(57.759625, 40.942470),  // Главный Корпус
                new LatLng(57.736841, 40.920328),  // Е Корпус
                new LatLng(57.761681, 40.940083),  // Б Корпус
                new LatLng(57.760810, 40.940021),  // В Корпус
                new LatLng(57.760810, 40.940021),  // Д Корпус
                new LatLng(57.766919, 40.918577),  // А1 Корпус
                new LatLng(57.767411, 40.917096),  // Г1 Корпус
                new LatLng(57.767802, 40.917167),  // В1 Корпус
                new LatLng(57.768314, 40.915687),  // Б1 Корпус
                new LatLng(57.778410, 40.913353),  // Спортивный Корпус
                new LatLng(57.800863, 41.003536)   // ИПП Корпус
        );

        List<String> titles = Arrays.asList(
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

        for (int i = 0; i < locations.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(locations.get(i))
                    .title(titles.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)) // ← кастомная иконка
            );
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locations.get(0), 14));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        enableMyLocation();
        startLocationUpdates();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        }, Looper.getMainLooper());
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
