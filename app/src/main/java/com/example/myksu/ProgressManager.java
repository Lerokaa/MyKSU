package com.example.myksu;

import android.content.Context;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProgressManager {
    private static ProgressManager instance;
    private final FlagsManager flagsManager;
    private final Gson gson = new Gson();
    private static final String PROGRESS_FILE = "game_progress.json";

    private ProgressManager() {
        flagsManager = new FlagsManager();
    }

    public static synchronized ProgressManager getInstance() {
        if (instance == null) {
            instance = new ProgressManager();
        }
        return instance;
    }

    // ===== Логика приложения =====
    public boolean isBuildingFullyCompleted(int buildingId) {
        return flagsManager.isBuildingDialogCompleted(buildingId) &&
                flagsManager.isBuildingMiniGameCompleted(buildingId);
    }

    public boolean areAllBuildingsCompleted() {
        for (int i = 1; i <= 11; i++) {
            if (!isBuildingFullyCompleted(i)) return false;
        }
        return true;
    }

    public boolean areAllDormitoriesCompleted() {
        for (int i = 1; i <= 6; i++) {
            if (!flagsManager.isDormitoryInfoViewed(i)) return false;
        }
        return true;
    }

    public int getCompletedBuildingsCount() {
        int count = 0;
        for (int i = 1; i <= 11; i++) {
            if (isBuildingFullyCompleted(i)) count++;
        }
        return count;
    }

    /**
    * Возвращает массив статусов завершения зданий
    * @return массив из 11 булевых значений (true если здание завершено)
    */
    public boolean[] getBuildingsCompletionStatus() {
        boolean[] statuses = new boolean[11];
        for (int i = 0; i < 11; i++) {
            statuses[i] = isBuildingFullyCompleted(i + 1); // +1 потому что здания нумеруются с 1
        }
        return statuses;
    }

    /**
     * Возвращает массив статусов просмотра информации об общежитиях
     * @return массив из 6 булевых значений (true если информация просмотрена)
     */
    public boolean[] getDormitoriesViewStatus() {
        boolean[] statuses = new boolean[6];
        for (int i = 0; i < 6; i++) {
            statuses[i] = flagsManager.isDormitoryInfoViewed(i + 1); // +1 потому что общежития нумеруются с 1
        }
        return statuses;
    }

    public void completeGameBuilding(int buildingId) {
        flagsManager.setBuildingMiniGameCompleted(buildingId);
    }

    public boolean isGameBuilding(int buildingId) {
        return flagsManager.isBuildingMiniGameCompleted(buildingId);
    }

    public boolean isWasBuildingDialog(int buildingId) {
        return flagsManager.isBuildingDialogCompleted(buildingId);
    }

    public void completeBuildingDialog(int buildingId) {
        flagsManager.setBuildingDialogCompleted(buildingId);
    }

    public void isDormitoryInfoViewed(int dormitoryId) {
        flagsManager.setDormitoryInfoViewed(dormitoryId);
    }


    // ===== Работа с сохранениями =====
    public void saveProgress(Context context) {
        try (FileOutputStream fos = context.openFileOutput(PROGRESS_FILE, Context.MODE_PRIVATE)) {
            String json = gson.toJson(flagsManager);
            fos.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadProgress(Context context) {
        try (FileInputStream fis = context.openFileInput(PROGRESS_FILE)) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = fis.read()) != -1) {
                sb.append((char) ch);
            }
            FlagsManager loaded = gson.fromJson(sb.toString(), FlagsManager.class);
            copyFlags(loaded);
        } catch (IOException e) {
            // Первый запуск или ошибка чтения - используем дефолтные значения
        }
    }

    private void copyFlags(FlagsManager source) {
        // Сначала сбрасываем все флаги
        flagsManager.resetAllFlags();

        // Устанавливаем только нужные флаги true из source
        for (int i = 1; i <= 11; i++) {
            if (source.isBuildingDialogCompleted(i)) {
                flagsManager.setBuildingDialogCompleted(i);
            }
            if (source.isBuildingMiniGameCompleted(i)) {
                flagsManager.setBuildingMiniGameCompleted(i);
            }
        }

        for (int i = 1; i <= 6; i++) {
            if (source.isDormitoryInfoViewed(i)) {
                flagsManager.setDormitoryInfoViewed(i);
            }
        }
    }

    public void resetAllProgress(Context context) {
        flagsManager.resetAllFlags();
        // Сохраняем сброшенные флаги
        saveProgress(context);
    }
}