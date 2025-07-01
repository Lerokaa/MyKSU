package com.example.myksu;

import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {

    ProgressManager progressManager;
    List<Boolean> progressListBool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        progressManager = ProgressManager.getInstance();
        progressManager.loadProgress(this);

        // Настройка кнопки "Назад"
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Закрывает текущую Activity и возвращает на предыдущую

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        // Получаем список достижений
        List<Achievement> allAchievements  = AchievementDataManager.getAllAchievements();

        progressListBool = new ArrayList<>();

        boolean[] buildingsProgress = progressManager.getBuildingsCompletionStatus();
        for (boolean status : buildingsProgress) {
            progressListBool.add(status);
        }
        progressListBool.add(progressManager.areAllBuildingsCompleted());
        boolean[] dormsProgress = progressManager.getDormitoriesViewStatus();
        for (boolean status : dormsProgress) {
            progressListBool.add(status);
        }
        progressListBool.add(progressManager.areAllDormitoriesCompleted());

        // Синхронизируем элементы с индексами 4 и 7
        if (progressListBool.size() > 7) { // Проверяем, что оба индекса существуют
            boolean element4 = progressListBool.get(4);
            boolean element7 = progressListBool.get(7);

            // Если хотя бы один true, устанавливаем оба в true
            if (element4 || element7) {
                progressListBool.set(4, true);
                progressListBool.set(7, true);
            }
        }

        // Фильтруем достижения, оставляем только те, где progressListBool.get(i) == true
        List<Achievement> completedAchievements = new ArrayList<>();
        for (int i = 0; i < Math.min(allAchievements.size(), progressListBool.size()); i++) {
            if (progressListBool.get(i)) {
                completedAchievements.add(allAchievements.get(i));
            }
        }

        // Настраиваем RecyclerView только с выполненными достижениями
        RecyclerView recyclerView = findViewById(R.id.achievementsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AchievementAdapter(completedAchievements));

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