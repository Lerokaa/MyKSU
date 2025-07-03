package com.example.myksu;

import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerManager {
    private static final float PROXIMITY_RADIUS = 52;
    private final MapActivity activity;
    private GoogleMap mMap;
    private final Map<Marker, Boolean> buildingMarkers = new HashMap<>();
    private final Map<Marker, Boolean> dormitoryMarkers = new HashMap<>();
    private final Map<Marker, Boolean> clickedMarkers = new HashMap<>();
    private final Map<Marker, Integer> buildingIds = new HashMap<>();
    private Marker currentSelectedMarker;
    private ProgressManager progressManager;

    public MarkerManager(MapActivity activity) {
        this.activity = activity;
        this.progressManager = activity.getProgressManager();
    }

    public void initWithMap(GoogleMap map) {
        this.mMap = map;
    }

    public void addBuildingMarkers() {
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
                int iconRes = progressManager.isWasBuildingDialog(buildingId)
                        ? R.drawable.btn_icons_marker
                        : R.drawable.btn_icons_non_marker;

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
                Log.e("MarkerManager", "Error adding building marker: " + e.getMessage());
            }
        }
    }

    public void addDormitoryMarkers() {
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
                Log.e("MarkerManager", "Error adding dormitory marker: " + e.getMessage());
            }
        }
    }

    public boolean hasBuildingMarkers() {
        return !buildingMarkers.isEmpty();
    }

    public boolean handleMarkerClick(Marker marker) {
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

                if (wasDialogCompleted) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));
                    Intent intent = new Intent(activity, InformationAboutKorpus.class);
                    intent.putExtra("BUILDING_ID", buildingId);
                    activity.startActivity(intent);
                } else if (Boolean.TRUE.equals(buildingMarkers.get(marker))) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));
                    Intent intent = new Intent(activity, CharactersDialogActivity.class);
                    intent.putExtra("DIALOG_ID", buildingId);
                    activity.startActivity(intent);
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked));
                    activity.getDialogManager().showBuildingDialog(marker);
                }
            } else if (dormitoryMarkers.containsKey(marker)) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_clicked_two));
                String title = marker.getTitle();
                int selectedDormitoryId = Integer.parseInt(title.replaceAll("[^0-9]", ""));
                activity.getDialogManager().showDormitoryDialog(selectedDormitoryId);
            }

            marker.showInfoWindow();
            return true;
        } catch (Exception e) {
            Log.e("MarkerManager", "Error in handleMarkerClick: " + e.getMessage());
            return false;
        }
    }

    public void checkProximityToBuildings(LatLng userLocation) {
        if (userLocation == null || mMap == null) {
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
                        userLocation.latitude,
                        userLocation.longitude,
                        marker.getPosition().latitude,
                        marker.getPosition().longitude,
                        results
                );

                if (results[0] <= PROXIMITY_RADIUS) {
                    if (!entry.getValue()) {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_marker_selected));
                        buildingMarkers.put(marker, true);
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, "Нажми на корпус, чтобы узнать о нем больше", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    if (entry.getValue()) {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.btn_icons_non_marker));
                        buildingMarkers.put(marker, false);
                    }
                }
            } catch (Exception e) {
                Log.e("MarkerManager", "Error checking proximity: " + e.getMessage());
            }
        }
    }

    private void resetMarkerIcon(Marker marker) {
        if (marker == null) {
            return;
        }

        try {
            if (buildingMarkers.containsKey(marker)) {
                int buildingId = buildingIds.get(marker);

                if (progressManager.isWasBuildingDialog(buildingId)) {
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
            Log.e("MarkerManager", "Error resetting marker icon: " + e.getMessage());
        }
    }
}