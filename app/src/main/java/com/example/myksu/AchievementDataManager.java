package com.example.myksu;

import java.util.ArrayList;
import java.util.List;

public class AchievementDataManager {
    private static List<Achievement> achievements = new ArrayList<>();

    static {
        // Инициализация всех 19 достижений
        achievements.add(new Achievement("Первый Шаг в КГУ", "Посетить Главный корпус КГУ", R.drawable.icons_ach_a1));
        achievements.add(new Achievement("Исследователь Корпуса А1", "Посетить корпус А1 (ул. 1 Мая, 14) и узнать о нем", R.drawable.icons_ach_a1));
        achievements.add(new Achievement("Исследователь Корпуса Б", "Посетить корпус Б (ул. Ивановская, 24А) и узнать о нем", R.drawable.icons_ach_b));
        achievements.add(new Achievement("Исследователь Корпуса Б1", "Посетить корпус Б1 (ул. Пятницкая, 2) и узнать о нем", R.drawable.icons_ach_b1));
        achievements.add(new Achievement("Исследователь Корпуса В", "Посетить корпус В (ул. Ивановская, 24А) и узнать о нем", R.drawable.icons_ach_v));
        achievements.add(new Achievement("Исследователь Корпуса В1", "Посетить корпус В1 (ул. 1 Мая, 14А) и узнать о нем", R.drawable.icons_ach_v1));
        achievements.add(new Achievement("Исследователь Корпуса Г1", "Посетить корпус Г1 (ул. 1 Мая, 16) и узнать о нем", R.drawable.icons_ach_g1));
        achievements.add(new Achievement("Исследователь Корпуса Д", "Посетить корпус Д (ул. Ивановская, 24А) и узнать о нем", R.drawable.icons_ach_d));
        achievements.add(new Achievement("Исследователь Корпуса E", "Посетить корпус Е (ул. Малышковская, 4) и узнать о нем", R.drawable.icons_ach_e));
        achievements.add(new Achievement("Спортивный Дух", "Посетить Спортивный корпус (ул. Симановского, 69А) и узнать о нем", R.drawable.icons_ach_s));
        achievements.add(new Achievement("Финишная Прямая", "Посетить корпус ИПП (поселок Новый, 1) и завершить основной квест", R.drawable.icons_ach_i));
        achievements.add(new Achievement("Полный Путь", "Посетить все 11 корпусов КГУ", R.drawable.icons_ach_pp));
        achievements.add(new Achievement("Любознатель: Общежитие 1", "прочитать информацию об Общежитии 1", R.drawable.icons_ach_o1));
        achievements.add(new Achievement("Любознатель: Общежитие 2", "прочитать информацию об Общежитии 2", R.drawable.icons_ach_o2));
        achievements.add(new Achievement("Любознатель: Общежитие 3", "прочитать информацию об Общежитии 3", R.drawable.icons_ach_o3));
        achievements.add(new Achievement("Любознатель: Общежитие 4", "прочитать информацию об Общежитии 4", R.drawable.icons_ach_o4));
        achievements.add(new Achievement("Любознатель: Общежитие 5", "прочитать информацию об Общежитии 5", R.drawable.icons_ach_o5));
        achievements.add(new Achievement("Любознатель: Общежитие 6", "прочитать информацию об Общежитии 6", R.drawable.icons_ach_o6));
        achievements.add(new Achievement("Знаток Общежитий", "прочитать информацию обо всех общежитиях", R.drawable.icons_ach_zo));
    }

    public static List<Achievement> getAllAchievements() {
        return achievements;
    }

    public static Achievement getAchievement(int index) {
        if (index >= 0 && index < achievements.size()) {
            return achievements.get(index);
        }
        return null;
    }
}