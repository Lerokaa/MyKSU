package com.example.myksu;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class ForTestGame extends AppCompatActivity {

    int id;
    ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.for_test_game);

        id = getIntent().getIntExtra("id", 1);
        progressManager = ProgressManager.getInstance();

        setupButtonListeners();
        // Запуск музыки
        MusicManager.startMusic(this, R.raw.your_music);
    }

    private void setupButtonListeners() {
        // Настройка кнопки "Назад"
        ImageButton backButton = findViewById(R.id.navButton);
        backButton.setOnClickListener(v -> finish()); // Закрывает текущую Activity и возвращает на предыдущую

        // Настройка кнопки "Settings"
        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> showSettingsDialog());
        }

        // Настройка кнопки достижений
        ImageButton continueButton = findViewById(R.id.btnHint);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AchievementActivity.class);
            startActivity(intent);
        });

        TextView corp = findViewById(R.id.titleText);
        corp.append(String.valueOf(id));

        ImageButton startButton = findViewById(R.id.startButton);
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                progressManager.completeGameBuilding(id);
                progressManager.saveProgress(this);
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
            });
        }
    }
    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);
        settingsDialog.setCancelable(true);

        Window window = settingsDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (315 * getResources().getDisplayMetrics().density);
            lp.height = (int) (210 * getResources().getDisplayMetrics().density);
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> settingsDialog.dismiss());
        }

        SeekBar volumeSeekBar = settingsDialog.findViewById(R.id.volumeSeekBar);
        if (volumeSeekBar != null) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
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
            }
        }

        ImageButton exitButton = settingsDialog.findViewById(R.id.exitButton);
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                settingsDialog.dismiss();
                finishAffinity();
                // System.exit(0); // Avoid using this as it's not recommended
            });
        }

        settingsDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.resumeMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.stopMusic();
    }
}
