package com.example.myksu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class LocationManager {
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private final Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentUserLocation;
    private GoogleMap mMap;
    private MarkerManager markerManager;

    public LocationManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void initWithMap(GoogleMap map) {
        this.mMap = map;
    }

    public void setMarkerManager(MarkerManager markerManager) {
        this.markerManager = markerManager;
    }

    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    Log.e("LocationManager", "SecurityException in enableMyLocation: " + e.getMessage());
                }
            }
        } else {
            ActivityCompat.requestPermissions((MapActivity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
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
                        if (markerManager != null) {
                            markerManager.checkProximityToBuildings(currentUserLocation);
                        }
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("LocationManager", "SecurityException in startLocationUpdates: " + e.getMessage());
        } catch (Exception e) {
            Log.e("LocationManager", "Error starting location updates: " + e.getMessage());
        }
    }

    public void centerOnUserLocation() {
        if (currentUserLocation != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 16));
        } else {
            Toast.makeText(context, "Местоположение не получено", Toast.LENGTH_SHORT).show();
            requestLastKnownLocation();
        }
    }

    public void requestLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 16));
                        }
                    }
                });
    }

    public LatLng getCurrentUserLocation() {
        return currentUserLocation;
    }
}