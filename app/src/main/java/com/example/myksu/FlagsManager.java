package com.example.myksu;

public class FlagsManager {
    // Модель данных для корпуса
    public static class Building {
        private boolean isDialogCompleted;
        private boolean isMiniGameCompleted;

        public Building() {
            this.isDialogCompleted = false;
            this.isMiniGameCompleted = false;
        }

        public boolean isDialogCompleted() {
            return isDialogCompleted;
        }

        public void setDialogCompleted(boolean dialogCompleted) {
            isDialogCompleted = dialogCompleted;
        }

        public boolean isMiniGameCompleted() {
            return isMiniGameCompleted;
        }

        public void setMiniGameCompleted(boolean miniGameCompleted) {
            isMiniGameCompleted = miniGameCompleted;
        }
    }

    // Модель данных для общежития
    public static class Dormitory {
        private boolean isInfoViewed;

        public Dormitory() {
            this.isInfoViewed = false;
        }

        public boolean isInfoViewed() {
            return isInfoViewed;
        }

        public void setInfoViewed(boolean infoViewed) {
            isInfoViewed = infoViewed;
        }
    }

    // Коллекции для хранения данных (индексы 1-11 для корпусов, 1-6 для общежитий)
    private final Building[] buildings;
    private final Dormitory[] dormitories;

    public FlagsManager() {
        // Инициализируем 11 корпусов (индексы 1-11)
        buildings = new Building[12]; // 0-й элемент не используется
        for (int i = 1; i <= 11; i++) {
            buildings[i] = new Building();
        }

        // Инициализируем 6 общежитий (индексы 1-6)
        dormitories = new Dormitory[7]; // 0-й элемент не используется
        for (int i = 1; i <= 6; i++) {
            dormitories[i] = new Dormitory();
        }
    }

    // Отметка что диалог пройден, ставится только один раз, сбрасывается только при полном сбросе
    public void setBuildingDialogCompleted(int buildingId) {
        if (isValidBuildingId(buildingId)) {
            buildings[buildingId].setDialogCompleted(true);
        }
    }

    //проверка что диалог пройден
    public boolean isBuildingDialogCompleted(int buildingId) {
        if (isValidBuildingId(buildingId)) {
            return buildings[buildingId].isDialogCompleted();
        }
        return false;
    }

    //отметка что игра пройдена, так как и диалог только один раз ставится
    public void setBuildingMiniGameCompleted(int buildingId) {
        if (isValidBuildingId(buildingId)) {
            buildings[buildingId].setMiniGameCompleted(true);
        }
    }

    //проверка что игра пройдена
    public boolean isBuildingMiniGameCompleted(int buildingId) {
        if (isValidBuildingId(buildingId)) {
            return buildings[buildingId].isMiniGameCompleted();
        }
        return false;
    }

    // отметка что общага просмотрена, ставится один раз
    public void setDormitoryInfoViewed(int dormitoryId) {
        if (isValidDormitoryId(dormitoryId)) {
            dormitories[dormitoryId].setInfoViewed(true);
        }
    }

    //проверка на просмотр общаги
    public boolean isDormitoryInfoViewed(int dormitoryId) {
        if (isValidDormitoryId(dormitoryId)) {
            return dormitories[dormitoryId].isInfoViewed();
        }
        return false;
    }

    // Валидация ID (теперь от 1 до N)
    private boolean isValidBuildingId(int id) {
        return id >= 1 && id <= 11;
    }

    private boolean isValidDormitoryId(int id) {
        return id >= 1 && id <= 6;
    }

    // Дополнительные методы для сброса флагов
    public void resetBuildingFlags(int buildingId) {
        if (isValidBuildingId(buildingId)) {
            buildings[buildingId].setDialogCompleted(false);
            buildings[buildingId].setMiniGameCompleted(false);
        }
    }

    public void resetDormitoryFlag(int dormitoryId) {
        if (isValidDormitoryId(dormitoryId)) {
            dormitories[dormitoryId].setInfoViewed(false);
        }
    }

    public void resetAllFlags() {
        // Сбрасываем флаги для всех корпусов (1-11)
        for (int i = 1; i <= 11; i++) {
            buildings[i].setDialogCompleted(false);
            buildings[i].setMiniGameCompleted(false);
        }

        // Сбрасываем флаги для всех общежитий (1-6)
        for (int i = 1; i <= 6; i++) {
            dormitories[i].setInfoViewed(false);
        }
    }

}