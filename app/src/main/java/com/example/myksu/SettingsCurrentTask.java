package com.example.myksu;

import android.content.Context;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class SettingsCurrentTask {

    private TextView textView;
    ProgressManager progressManager;
    Context context;

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


    public SettingsCurrentTask(TextView te)
    {
        progressManager = ProgressManager.getInstance();
        progressManager.loadProgress(context);
        textView = te;
    }


//    private List<String> checkComlectedCorpus()
//    {
//
//    }

}
