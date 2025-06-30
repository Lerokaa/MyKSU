package com.example.myksu;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class DialoguesData {

    public static class Dialog {
        private int id;
        private String character;
        private List<String> pic;
        private List<String> phrases;

        // Геттеры
        public int getId() { return id; }
        public String getCharacter() { return character; }
        public List<String> getPic() { return pic; }
        public List<String> getPhrases() { return phrases; }
    }

    // Метод для поиска диалога по ID
    public static Dialog parseSingleDialog(InputStream inputStream, int targetId) {
        Gson gson = new GsonBuilder().create();
        InputStreamReader reader = new InputStreamReader(inputStream);

        // Парсим весь JSON и сразу ищем нужный диалог
        DialogsResponse response = gson.fromJson(reader, DialogsResponse.class);
        if (response.getDialogs() != null) {
            for (Dialog dialog : response.getDialogs()) {
                if (dialog.getId() == targetId) {
                    return dialog; // Возвращаем как только нашли
                }
            }
        }
        return null; // Если не найдено
    }

    private static class DialogsResponse {
        private List<Dialog> dialogs;

        public List<Dialog> getDialogs() {
            return dialogs;
        }
    }
}
