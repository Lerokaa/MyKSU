package com.example.myksu;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
        imageView.setImageResource(R.drawable.placeholder_image);

        // Настройка заголовков
        TextView titleText = view.findViewById(R.id.titleText);
        titleText.setText("Общага 1");

        TextView subtitleText = view.findViewById(R.id.subtitleText);
        subtitleText.setText("(1 переулок Воскресенский, 17)");

        // Настройка основного текста
        TextView contentText = view.findViewById(R.id.contentText);
        contentText.setText("БЕББЕ66ебееБУБУБбля66я6я6П УПУПпуппепепепеппупупБЕББЕ 66ебееБУБУБбля66я6я6ПУПУПпуппепепепеппупупывывывывывы");

        // Настройка кнопок
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
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 90% ширины экрана
            int horizontalMargin = (int) (getResources().getDisplayMetrics().widthPixels * 0.0001);
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setDimAmount(0.5f); // Затемнение фона

            // Установка отступов
            View view = getView();
            if (view != null) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.MarginLayoutParams(width, height);
                }
                params.leftMargin = horizontalMargin;
                params.rightMargin = horizontalMargin;
                view.setLayoutParams(params);
            }
        }
    }

    public static CustomDialogObshaga newInstance() {
        return new CustomDialogObshaga();
    }
}