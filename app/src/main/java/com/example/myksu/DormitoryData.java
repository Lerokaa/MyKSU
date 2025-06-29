package com.example.myksu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class DormitoryData {
    // Модель Infrastructure
    public static class Infrastructure {
        @SerializedName("first_floor")
        private String firstFloor;

        @SerializedName("block_structure")
        private String blockStructure;

        public String getFirstFloor() { return firstFloor; }

        // Возвращает часть до двоеточия (с проверкой на null и отсутствие разделителя)
        public String getBlockStructureTitle() {
            if (blockStructure == null) return "";

            int colonIndex = blockStructure.indexOf(':');
            return colonIndex >= 0
                    ? blockStructure.substring(0, colonIndex).trim()
                    : blockStructure.trim();
        }

        // Возвращает часть после двоеточия (с проверкой)
        public String getBlockStructureDescription() {
            if (blockStructure == null) return "";

            int colonIndex = blockStructure.indexOf(':');
            return colonIndex >= 0
                    ? blockStructure.substring(colonIndex + 1).trim()
                    : "";
        }
    }

    // Модель LivingConditions
    public static class LivingConditions {
        private String message;

        @SerializedName("per_floor")
        private String perFloor;

        public String getMessage() { return message; }
        public String getPerFloor() { return perFloor; }
    }

    // Модель Dormitory
    public static class Dormitory {
        private int id;
        private String name;
        private String address;
        private String commandant;
        private String phone;
        private List<String> institutes;
        private String building;
        private Infrastructure infrastructure;

        @SerializedName("living_conditions")
        private LivingConditions livingConditions;

        private List<String> photos;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getCommandant() { return commandant; }
        public String getPhone() { return phone; }
        public List<String> getInstitutes() { return institutes; }
        public String getBuilding() { return building; }
        public Infrastructure getInfrastructure() { return infrastructure; }
        public LivingConditions getLivingConditions() { return livingConditions; }
        public List<String> getPhotos() { return photos; }
    }

    // Модель DormitoriesResponse
    private static class DormitoriesResponse {
        private List<Dormitory> dormitories;

        public List<Dormitory> getDormitories() {
            return dormitories;
        }
    }

    // Парсинг всего списка
    public static List<Dormitory> parseDormitories(InputStream inputStream) {
        Gson gson = new GsonBuilder().create();
        InputStreamReader reader = new InputStreamReader(inputStream);

        DormitoriesResponse response = gson.fromJson(reader, DormitoriesResponse.class);
        return response.getDormitories();
    }

    // Поиск общежития по ID
    public static Dormitory findDormitoryById(List<Dormitory> dormitories, int id) {
        if (dormitories == null) {
            return null;
        }

        for (Dormitory dorm : dormitories) {
            if (dorm.getId() == id) {
                return dorm;
            }
        }
        return null; // Если не найдено
    }
}
