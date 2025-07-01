package com.example.myksu;

public class Achievement {
    private String name;
    private String description;
    private int imageResId;

    // Конструктор
    public Achievement(String name, String description, int imageResId) {
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}