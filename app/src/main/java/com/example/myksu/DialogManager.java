package com.example.myksu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class DialogManager {
    private final Context context;
    private final RouteManager routeManager;

    public DialogManager(Context context) {
        this.context = context;
        this.routeManager = ((MapActivity) context).getRouteManager();
    }

    public void showBuildingDialog(Marker marker) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
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
                    routeManager.buildRouteToBuilding(marker);
                    ((AlertDialog) v.getTag()).dismiss();
                } catch (Exception e) {
                    Log.e("DialogManager", "Error building route: " + e.getMessage());
                    Toast.makeText(context, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show();
                }
            });
            routeButton.setTag(builder.create());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            closeButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Log.e("DialogManager", "Error showing dialog: " + e.getMessage());
            Toast.makeText(context, "Ошибка отображения диалога", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDormitoryDialog(int dormitoryId) {
        CustomDialogObshaga dialog = CustomDialogObshaga.newInstance(dormitoryId);
        dialog.show(((MapActivity) context).getSupportFragmentManager(), "dormitory_dialog");
    }

    public void showSettingsDialog() {
        try {
            Dialog settingsDialog = new Dialog(context);
            settingsDialog.setContentView(R.layout.dialog_settings);

            settingsDialog.setTitle(null);
            settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Window window = settingsDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(window.getAttributes());
                lp.width = (int) (315 * context.getResources().getDisplayMetrics().density);
                lp.height = (int) (210 * context.getResources().getDisplayMetrics().density);
                lp.dimAmount = 0.7f;
                window.setAttributes(lp);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }

            ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(v -> settingsDialog.dismiss());

            SeekBar volumeSeekBar = settingsDialog.findViewById(R.id.volumeSeekBar);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

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
                ((MapActivity) context).finishAffinity();
                System.exit(0);
            });

            settingsDialog.show();
        } catch (Exception e) {
            Log.e("DialogManager", "Error showing settings dialog: " + e.getMessage());
            Toast.makeText(context, "Ошибка отображения настроек", Toast.LENGTH_SHORT).show();
        }
    }
}