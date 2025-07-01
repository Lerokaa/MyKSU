package com.example.myksu;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        // Настройка кнопки "Начать"
        ImageButton startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Настройка кнопки "Продолжить"
        ImageButton continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Настройка кнопки "Выход"
        ImageButton exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> finishAffinity());
    }

    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);

        // Убираем стандартный заголовок и делаем прозрачный фон
        settingsDialog.setTitle(null);
        settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Настраиваем размеры диалога и затемнение
        Window window = settingsDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            // Устанавливаем фиксированные размеры (315x210 dp)
            lp.width = (int) (315 * getResources().getDisplayMetrics().density);
            lp.height = (int) (210 * getResources().getDisplayMetrics().density);
            // Устанавливаем уровень затемнения (0.7f - 70% затемнения)
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            // Включаем флаг затемнения
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        // Кнопка закрытия
        ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> settingsDialog.dismiss());

        // Настройка SeekBar для громкости
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

        // Получаем кнопку выхода
        ImageButton exitButton = settingsDialog.findViewById(R.id.exitButton);

        // Обработчик клика для выхода из приложения
        View.OnClickListener exitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Закрываем диалог
                settingsDialog.dismiss();

                // Полностью закрываем приложение
                finishAffinity(); // Закрывает все Activity
                System.exit(0);   // Завершает процесс
            }
        };

        // Назначаем обработчик на кнопку
        exitButton.setOnClickListener(exitListener);

        settingsDialog.show();
    }
}