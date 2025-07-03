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
            "Главном корпусе",
            "А1 корпусе",
            "Б корпусе",
            "Б1 корпусе",
            "В корпусе",
            "В1 корпусе",
            "Г1 корпусе",
            "Д корпусе",
            "Е корпусе",
            "Спортивном корпусе",
            "ИПП корпусе"
    );


    public SettingsCurrentTask(TextView te, Context ctx)
    {
        context = ctx;
        progressManager = ProgressManager.getInstance();
        progressManager.loadProgress(context);
        textView = te;
    }


    private int isTaskCorpus()
    {
        for (int i = 0; i < buildingTitles.size(); i++)
        {
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
        id = isTaskCorpus();
        String s = "Вы все прошли";
        if (id == 16)
        {
            textView.setText(s);
        }
        else
        {
            textView.setText(String.format("Пройдите игру в %s", buildingTitles.get(id)));
        }

    }

}
