package com.example.myksu;

import android.os.Bundle;

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
}