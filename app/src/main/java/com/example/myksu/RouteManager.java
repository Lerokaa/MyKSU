package com.example.myksu;

import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteManager {
    private static final String DIRECTIONS_API_KEY = "5b3ce3597851110001cf624884b1501b04444c8f9d22b4c100ef261c";
    private static final String TAG = "RouteManager";
    private final MapActivity activity;
    private GoogleMap mMap;
    private Polyline currentRoute;
    private final OkHttpClient httpClient = new OkHttpClient();

    public RouteManager(MapActivity activity) {
        this.activity = activity;
    }

    public void initWithMap(GoogleMap map) {
        this.mMap = map;
    }

    public void buildRouteToBuilding(Marker destinationMarker) {
        LatLng currentUserLocation = activity.getLocationManager().getCurrentUserLocation();

        if (currentUserLocation == null) {
            Log.e(TAG, "Current user location is null");
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show());
            activity.getLocationManager().requestLastKnownLocation();
            return;
        }

        if (destinationMarker == null || !destinationMarker.isVisible()) {
            Log.e(TAG, "Invalid destination marker");
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Неверный маркер назначения", Toast.LENGTH_SHORT).show());
            return;
        }

        // Clear previous route if exists
        if (currentRoute != null) {
            currentRoute.remove();
        }

        // Format coordinates as lon,lat for OpenRouteService
        String startPoint = currentUserLocation.longitude + "," + currentUserLocation.latitude;
        String endPoint = destinationMarker.getPosition().longitude + "," + destinationMarker.getPosition().latitude;

        String url = "https://api.openrouteservice.org/v2/directions/foot-walking?" +
                "api_key=" + DIRECTIONS_API_KEY +
                "&start=" + startPoint +
                "&end=" + endPoint;

        Log.d(TAG, "Requesting route from: " + startPoint + " to " + endPoint);
        Log.d(TAG, "API URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json, application/geo+json")
                .addHeader("Authorization", DIRECTIONS_API_KEY)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Route request failed", e);
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Ошибка подключения: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "null";
                        Log.e(TAG, "Unsuccessful response: " + response.code() + " - " + errorBody);
                        throw new IOException("HTTP error code: " + response.code());
                    }

                    String responseData = response.body().string();
                    Log.d(TAG, "API Response: " + responseData);

                    JSONObject json = new JSONObject(responseData);
                    JSONArray features = json.getJSONArray("features");

                    if (features.length() == 0) {
                        throw new Exception("No features in response");
                    }

                    JSONObject route = features.getJSONObject(0);
                    JSONObject geometry = route.getJSONObject("geometry");

                    if (!geometry.getString("type").equals("LineString")) {
                        throw new Exception("Unsupported geometry type: " + geometry.getString("type"));
                    }

                    JSONArray coords = geometry.getJSONArray("coordinates");
                    List<LatLng> path = new ArrayList<>();

                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray coord = coords.getJSONArray(i);
                        // Note: OpenRouteService returns [lon, lat] order
                        double lon = coord.getDouble(0);
                        double lat = coord.getDouble(1);
                        path.add(new LatLng(lat, lon));
                        Log.d(TAG, "Route point " + i + ": " + lat + ", " + lon);
                    }

                    if (path.isEmpty()) {
                        Log.e(TAG, "Empty path decoded");
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, "Не удалось декодировать путь", Toast.LENGTH_LONG).show());
                        return;
                    }

                    activity.runOnUiThread(() -> drawRouteOnMap(path));

                } catch (Exception e) {
                    Log.e(TAG, "Error processing route", e);
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "Ошибка обработки маршрута: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    private void drawRouteOnMap(List<LatLng> path) {
        try {
            // Clear previous route if exists
            if (currentRoute != null) {
                currentRoute.remove();
            }

            PolylineOptions options = new PolylineOptions()
                    .addAll(path)
                    .width(12)
                    .color(Color.parseColor("#3F51B5"))
                    .geodesic(true);

            currentRoute = mMap.addPolyline(options);
            Log.d(TAG, "Route drawn with " + path.size() + " points");

            // Zoom to show the entire route
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : path) {
                builder.include(point);
            }

            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (Exception e) {
                Log.w(TAG, "Failed to fit bounds, using fallback zoom");
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 15));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error drawing route", e);
            Toast.makeText(activity, "Ошибка отрисовки маршрута", Toast.LENGTH_SHORT).show();
        }
    }
}