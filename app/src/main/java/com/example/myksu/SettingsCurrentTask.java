package com.example.myksu;

import android.content.Context;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class SettingsCurrentTask {

    private TextView textView;
    ProgressManager progressManager;
    Context context;
    int id =16;

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


    private int isTaskCorpus()
    {
        for (int i = 0; i == buildingTitles.size(); i++) {
            boolean isComp = progressManager.isBuildingFullyCompleted(i+1);
            if (!isComp)
            { id = i;
                break;
            }
        }
        return id;
    }

    public void SetTask()
    {
        String s = "Вы все прошли";
        if (id == 16)
        {
            textView.setText(s);
        }
        else
        {
            textView.setText(buildingTitles.get(id));
        }

    }

}
