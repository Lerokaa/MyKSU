package com.example.myksu;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CustomDialogObshaga extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_obshaga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Настройка изображения
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.other_placeholder_image);

        // Настройка заголовков
        TextView titleText = view.findViewById(R.id.name);
        titleText.setText("Общага №1");

        TextView subtitleText = view.findViewById(R.id.address);
        subtitleText.setText("(1 переулок Воскресенский, 17)");

        // Добавленные поля
        TextView commendantText = view.findViewById(R.id.commandant);
        commendantText.setText("Комендант: Иванова И.И.");

        TextView phoneText = view.findViewById(R.id.phone);
        phoneText.setText("Телефон: +7 (123) 456-78-90");

        TextView instituteText = view.findViewById(R.id.institute);
        instituteText.setText("Институт: ИИТиТО");

        // Настройка кнопок (без изменений)
        ImageButton detailsButton = view.findViewById(R.id.detailsButton);
        detailsButton.setOnClickListener(v -> {
            // Обработка нажатия "Подробнее"
        });

        ImageButton routeButton = view.findViewById(R.id.routeButton);
        routeButton.setOnClickListener(v -> {
            // Обработка нажатия "Маршрут"
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Получаем размеры экрана
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            // Определяем размеры диалога в зависимости от ориентации
            int width = (int) (screenWidth * 0.9); // 90% ширины экрана
            int height;

            if (screenWidth > screenHeight) {
                // Ландшафтная ориентация
                height = (int) (screenHeight * 0.8); // 80% высоты экрана
            } else {
                // Портретная ориентация
                height = (int) (screenHeight * 0.7); // 70% высоты экрана
            }

            // Устанавливаем минимальные размеры для маленьких экранов
            int minWidth = (int) (300 * getResources().getDisplayMetrics().density);
            int minHeight = (int) (400 * getResources().getDisplayMetrics().density);

            width = Math.max(width, minWidth);
            height = Math.max(height, minHeight);

            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setDimAmount(0.5f); // Затемнение фона

            // Центрируем диалог на экране
            getDialog().getWindow().setGravity(Gravity.CENTER);
        }
    }

    public static CustomDialogObshaga newInstance() {
        return new CustomDialogObshaga();
    }
}