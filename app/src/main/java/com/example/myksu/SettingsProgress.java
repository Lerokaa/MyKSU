package com.example.myksu;

import android.content.Context;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class SettingsProgress {

    private final ProgressBar progressBar;
    ProgressManager progressManager;
    Context context;
    private static final int MAX_COUNT = 16;


    public SettingsProgress(ProgressBar pr, Context ctx) {
        context = ctx;
        progressManager = ProgressManager.getInstance();
        progressManager.loadProgress(context);
        progressBar = pr;
    }

    private int CountProgress()
    {
        int count = 0;

        count = progressManager.getCompletedBuildingsCount();
        count += progressManager.getCompletedDormitoriesCount();

        return count;
    }

    private int CalculateProgressPercentage()
    {
        int currentProgress = CountProgress();
        // Вычисляем процент, округляя до целого числа
        return (int) Math.round((currentProgress * 100.0) / MAX_COUNT);
    }

    public void SetProgressBar()
    {
        progressBar.setProgress(CalculateProgressPercentage());
    }
}
