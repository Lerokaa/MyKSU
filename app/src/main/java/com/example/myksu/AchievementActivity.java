package com.example.myksu;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        // Получаем список достижений
        List<Achievement> achievements = AchievementDataManager.getAllAchievements();
        Log.d("AchievementActivity", "Achievements count: " + achievements.size());

        // Настраиваем RecyclerView
        RecyclerView recyclerView = findViewById(R.id.achievementsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AchievementAdapter(achievements));
    }
}