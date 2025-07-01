package com.example.myksu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class BuildingData {

    public static class Building {
        private int id;
        private String shortName;
        private String fullName;
        private String address;
        private List<String> photos;
        private List<String> relatedDormitories;
        private List<String> institutes;
        private String have;
        private String history;

        // Геттеры
        public int getId() { return id; }
        public String getShortName() { return shortName; }
        public String getFullName() { return fullName; }
        public String getAddress() { return address; }
        public List<String> getPhotos() { return photos; }
        public List<String> getRelatedDormitories() { return relatedDormitories; }
        public List<String> getInstitutes() { return institutes; }
        public String getTitleHave() {
            if (have == null) return "";
            int colonIndex = have.indexOf(':');
            return colonIndex >= 0 ? have.substring(0, colonIndex).trim() : have;
        }
        public String getSubHave() {
            if (have == null) return "";
            int colonIndex = have.indexOf(':');
            return colonIndex >= 0 ? have.substring(colonIndex + 1).trim() : "";
        }
        public String getHistory() { return history; }
    }

    // Метод для поиска корпуса по ID
    public static Building parseSingleBuilding(InputStream inputStream, int targetId) {
        Gson gson = new GsonBuilder().create();
        InputStreamReader reader = new InputStreamReader(inputStream);

        // Парсим весь JSON и сразу ищем нужный корпус
        BuildingsResponse response = gson.fromJson(reader, BuildingsResponse.class);
        if (response.getBuildings() != null) {
            for (Building building : response.getBuildings()) {
                if (building.getId() == targetId) {
                    return building; // Возвращаем как только нашли
                }
            }
        }
        return null; // Если не найдено
    }

    private static class BuildingsResponse {
        private List<Building> buildings;

        public List<Building> getBuildings() {
            return buildings;
        }
    }
}
