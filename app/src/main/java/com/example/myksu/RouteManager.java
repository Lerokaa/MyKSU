package com.example.myksu;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteManager {
    private static final String DIRECTIONS_API_KEY = "5b3ce3597851110001cf624884b1501b04444c8f9d22b4c100ef261c";
    private static final String TAG = "RouteManager";
    private static final int TIMEOUT_SECONDS = 15;

    private final MapActivity activity;
    private GoogleMap mMap;
    private Polyline currentRoute;
    private final OkHttpClient httpClient;
    private ImageButton hideRouteButton;

    public RouteManager(MapActivity activity) {
        this.activity = activity;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    public void initWithMap(GoogleMap map) {
        this.mMap = map;
        initHideRouteButton();
    }

    private void initHideRouteButton() {
        hideRouteButton = activity.findViewById(R.id.btn_hide_route);
        hideRouteButton.setVisibility(View.GONE);
        hideRouteButton.setOnClickListener(v -> clearRoute());
    }

    public void clearRoute() {
        if (currentRoute != null) {
            currentRoute.remove();
            currentRoute = null;
        }
        if (hideRouteButton != null) {
            hideRouteButton.setVisibility(View.GONE);
        }
    }

    public void buildRouteToBuilding(Marker destinationMarker) {
        buildRoute(activity.getLocationManager().getCurrentUserLocation(),
                destinationMarker != null ? destinationMarker.getPosition() : null);
    }

    public void buildRouteToDormitory(LatLng dormitoryLocation) {
        buildRoute(activity.getLocationManager().getCurrentUserLocation(), dormitoryLocation);
    }

    private void buildRoute(LatLng startLocation, LatLng endLocation) {
        clearRoute();

        // Проверка входных данных
        if (startLocation == null) {
            showError("Не удалось определить ваше местоположение");
            activity.getLocationManager().requestLastKnownLocation();
            return;
        }

        if (endLocation == null) {
            showError("Неверные координаты назначения");
            return;
        }

        String startPoint = startLocation.longitude + "," + startLocation.latitude;
        String endPoint = endLocation.longitude + "," + endLocation.latitude;

        String url = "https://api.openrouteservice.org/v2/directions/foot-walking?" +
                "api_key=" + DIRECTIONS_API_KEY +
                "&start=" + startPoint +
                "&end=" + endPoint;

        Log.d(TAG, "Requesting route from: " + startPoint + " to " + endPoint);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json, application/geo+json")
                .addHeader("Authorization", DIRECTIONS_API_KEY)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Route request failed", e);
                showError("Ошибка сети: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "null";
                        Log.e(TAG, "API Error: " + response.code() + " - " + errorBody);
                        showError("Ошибка API: " + response.code());
                        return;
                    }

                    String responseData = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "API Response: " + responseData);

                    List<LatLng> path = parseRoute(responseData);
                    if (path == null || path.isEmpty()) {
                        showError("Не удалось построить маршрут");
                        return;
                    }

                    activity.runOnUiThread(() -> {
                        drawRoute(path);
                        if (hideRouteButton != null) {
                            hideRouteButton.setVisibility(View.VISIBLE);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error processing route", e);
                    showError("Ошибка обработки маршрута");
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    private List<LatLng> parseRoute(String jsonResponse) throws Exception {
        JSONObject json = new JSONObject(jsonResponse);
        JSONArray features = json.getJSONArray("features");

        if (features.length() == 0) {
            throw new Exception("No features in response");
        }

        JSONObject route = features.getJSONObject(0);
        JSONObject geometry = route.getJSONObject("geometry");

        if (!geometry.getString("type").equals("LineString")) {
            throw new Exception("Unsupported geometry type");
        }

        JSONArray coords = geometry.getJSONArray("coordinates");
        List<LatLng> path = new ArrayList<>();

        for (int i = 0; i < coords.length(); i++) {
            JSONArray coord = coords.getJSONArray(i);
            path.add(new LatLng(coord.getDouble(1), coord.getDouble(0)));
        }

        return path;
    }

    private void drawRoute(List<LatLng> path) {
        try {
            PolylineOptions options = new PolylineOptions()
                    .addAll(path)
                    .width(12)
                    .color(Color.parseColor("#3F51B5"))
                    .geodesic(true);

            currentRoute = mMap.addPolyline(options);
            zoomToRoute(path);

        } catch (Exception e) {
            Log.e(TAG, "Error drawing route", e);
            showError("Ошибка отрисовки маршрута");
        }
    }

    private void zoomToRoute(List<LatLng> path) {
        if (path == null || path.isEmpty()) return;

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : path) {
                builder.include(point);
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        } catch (Exception e) {
            Log.w(TAG, "Failed to fit bounds, using fallback zoom");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 15));
        }
    }

    private void showError(String message) {
        activity.runOnUiThread(() ->
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show());
    }
}