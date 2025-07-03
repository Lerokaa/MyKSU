package com.example.myksu;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.util.List;

public class CustomDialogObshaga extends DialogFragment {

    private static final String ARG_DORMITORY_ID = "dormitory_id";
    private int dormitoryId;
    ProgressManager progressManager;
    private LatLng dormitoryLocation;

    public static CustomDialogObshaga newInstance(int dormitoryId) {
        CustomDialogObshaga fragment = new CustomDialogObshaga();
        Bundle args = new Bundle();
        args.putInt(ARG_DORMITORY_ID, dormitoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dormitoryId = getArguments().getInt(ARG_DORMITORY_ID);
        }
        progressManager = ProgressManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_obshaga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Загружаем данные об общежитии
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.dormitories);
            DormitoryData.Dormitory dorm = DormitoryData.parseSingleDormitory(inputStream, dormitoryId);

            if (dorm != null) {
                // Настройка изображения
                ImageView imageView = view.findViewById(R.id.imageView);
                if (dorm.getPhotos() != null && !dorm.getPhotos().isEmpty()) {
                    String photoName = dorm.getPhotos().get(0).replace(".jpg", ""); // Удаляем расширение
                    int resId = getResources().getIdentifier(
                            photoName,
                            "drawable",
                            requireContext().getPackageName()
                    );

                    if (resId != 0) {
                        imageView.setImageResource(resId); // Загружаем напрямую
                    } else {
                        imageView.setImageResource(R.drawable.other_placeholder_image); // Заглушка
                        Log.e("ImageLoad", "Изображение не найдено: " + photoName);
                    }
                } else {
                    imageView.setImageResource(R.drawable.other_placeholder_image);
                }

                progressManager.isDormitoryInfoViewed(dorm.getId());
                progressManager.saveProgress(requireContext());
                Log.e("AchievementActivity", "obh");
                // Заполняем текстовые поля
                TextView Name = view.findViewById(R.id.name);
                Name.setText(dorm.getName());

                TextView Address = view.findViewById(R.id.address);
                Address.setText(dorm.getAddress());

                TextView commendantText = view.findViewById(R.id.commandant);
                commendantText.setText(getString(R.string.commandant_format, dorm.getCommandant()));

                TextView phoneText = view.findViewById(R.id.phone);
                phoneText.setText(getString(R.string.phone_format, dorm.getPhone()));

                TextView instituteText = view.findViewById(R.id.institute);
                if (dorm.getInstitutes() != null && !dorm.getInstitutes().isEmpty()) {
                    instituteText.setText(getString(R.string.institutes_format,
                            String.join(", ", dorm.getInstitutes())));
                } else {
                    instituteText.setVisibility(View.GONE);
                }

                // Получаем координаты общежития
                List<LatLng> dormitoryLocations = getDormitoryLocations();
                if (dormitoryId > 0 && dormitoryId <= dormitoryLocations.size()) {
                    dormitoryLocation = dormitoryLocations.get(dormitoryId - 1);
                }

                // Настройка кнопок
                ImageButton detailsButton = view.findViewById(R.id.detailsButton);
                detailsButton.setOnClickListener(v -> {
                    // Используем requireContext() вместо this
                    Intent intent = new Intent(requireActivity(), InformationAboutObshagi.class);
                    intent.putExtra("DORMITORY_ID", dormitoryId);
                    startActivity(intent);

                    // Закрываем диалог после перехода (опционально)
                    dismiss();
                });

                ImageButton routeButton = view.findViewById(R.id.routeButton);
                routeButton.setOnClickListener(v -> {
                    if (dormitoryLocation != null) {
                        if (getActivity() instanceof MapActivity) {
                            MapActivity mapActivity = (MapActivity) getActivity();
                            mapActivity.getRouteManager().buildRouteToDormitory(dormitoryLocation);
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Ошибка: активность не является MapActivity", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Не удалось определить расположение общежития", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Данные об общежитии не найдены", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private List<LatLng> getDormitoryLocations() {
        return Arrays.asList(
                new LatLng(57.754431, 40.952182), // Общежитие №1
                new LatLng(57.753951, 40.954221), // Общежитие №2
                new LatLng(57.736553, 40.920300), // Общежитие №3
                new LatLng(57.755104, 40.955613), // Общежитие №4
                new LatLng(57.755233, 40.954607), // Общежитие №5
                new LatLng(57.767923, 40.918962)  // Общежитие №6
        );
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