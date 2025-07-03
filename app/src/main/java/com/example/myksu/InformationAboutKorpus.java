package com.example.myksu;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InformationAboutKorpus extends AppCompatActivity {

    private ViewPager2 photosCarousel;
    private CarouselAdapter carouselAdapter;
    private final Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private static final long AUTO_SCROLL_DELAY = 3000;
    private BuildingData.Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_about_korpus);
        // Запуск музыки
        MusicManager.startMusic(this, R.raw.your_music);

        Log.e("BuildingData", "Error loading building data");

        // Получаем ID корпуса из Intent
        int buildingId = getIntent().getIntExtra("BUILDING_ID", 1); // 1 - значение по умолчанию

        // Загружаем данные о корпусе
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.buildings);
            building = BuildingData.parseSingleBuilding(inputStream, buildingId);
            inputStream.close();
        } catch (Exception e) {
            Log.e("BuildingData", "Error loading building data", e);
        }

        initUI();
        setupTextContent();
        loadData();
        setupCarousel();
        setupButtonListeners();
    }

    private void initUI() {
        photosCarousel = findViewById(R.id.photosCarousel);
    }

    private void loadData() {
        TextView addressText = findViewById(R.id.addressText);
        if (addressText != null && building != null) {
            addressText.setText(building.getShortName());
        } else if (addressText != null) {
            addressText.setText("Главный корпус КГУ");
        }
    }

    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();

        // Если есть фото в данных о корпусе
        if (building != null && building.getPhotos() != null && !building.getPhotos().isEmpty()) {
            for (String photoName : building.getPhotos()) {
                try {
                    // Получаем идентификатор ресурса по имени файла
                    int resId = getResources().getIdentifier(
                            photoName.toLowerCase().replace(".jpg", "").replace(".png", ""),
                            "drawable",
                            getPackageName()
                    );

                    if (resId != 0) {
                        images.add(resId);
                    } else {
                        Log.e("Carousel", "Image not found: " + photoName);
                        images.add(R.drawable.other_photo1); // Заглушка
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    images.add(R.drawable.other_photo1);
                }
            }
        }

        // Если фото нет в данных, используем дефолтные
        if (images.isEmpty()) {
            images.add(R.drawable.other_photo1);
            images.add(R.drawable.other_photo2);
            images.add(R.drawable.other_photo3);
        }

        carouselAdapter = new CarouselAdapter(this, images);
        photosCarousel.setAdapter(carouselAdapter);

        // Check if there are items before setting current item
        if (carouselAdapter.getItemCount() > 0) {
            int startPosition = carouselAdapter.getItemCount() / 2;
            photosCarousel.setCurrentItem(startPosition, false);
        }

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (photosCarousel.getAdapter() != null && photosCarousel.getAdapter().getItemCount() > 0) {
                    int nextItem = (photosCarousel.getCurrentItem() + 1) % carouselAdapter.getItemCount();
                    photosCarousel.setCurrentItem(nextItem, true);
                    autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
                }
            }
        };

        photosCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE && carouselAdapter.getItemCount() > 0) {
                    int itemCount = carouselAdapter.getItemCount();
                    int currentItem = photosCarousel.getCurrentItem();

                    if (currentItem == 0) {
                        photosCarousel.setCurrentItem(itemCount - 2, false);
                    } else if (currentItem == itemCount - 1) {
                        photosCarousel.setCurrentItem(1, false);
                    }
                }
            }
        });
    }

    private void setupButtonListeners() {
        // Настройка кнопки "Назад"
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Закрывает текущую Activity и возвращает на предыдущую

        // Настройка кнопки "Settings"
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> showSettingsDialog());
        }

        // Настройка кнопки достижений
        ImageButton continueButton = findViewById(R.id.achievementsButton);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AchievementActivity.class);
            startActivity(intent);
        });

        ImageButton startButton = findViewById(R.id.btn_start);
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
            });
        }
        ImageButton gameButton = findViewById(R.id.btn_continue);
        if (gameButton != null) {
            int buildingId = building.getId();

            gameButton.setOnClickListener(v -> {
                Intent intent;

                switch (buildingId) {
                    case 1:
                        intent = new Intent(this, GuessNumberActivity.class);
                        break;
                    case 2:
                        intent = new Intent(this, ForTestGame.class);
                        break;
                    case 3:
                        intent = new Intent(this, PuzzleActivity.class);
                        break;
                    case 4:
                        intent = new Intent(this, FlappyBirdActivity.class);
                        break;
                    case 5:
                        intent = new Intent(this, Game2048Activity.class);
                        break;
                    case 6:
                        intent = new Intent(this, ForTestGame.class);
                        break;
                    case 7:
                        intent = new Intent(this, ForTestGame.class);
                        break;
                    case 11:
                        intent = new Intent(this, MemoryGameActivity.class);
                        break;
                    case 9:
                        intent = new Intent(this, ColorConnectionActivity.class);
                        break;
                    case 10:
                        intent = new Intent(this, ForTestGame.class);
                        break;
                    default:
                        // Для всех остальных ID используем ForTestGame
                        intent = new Intent(this, ForTestGame.class);
                        intent.putExtra("id", buildingId);
                        break;
                }

                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);
        settingsDialog.setCancelable(true);

        Window window = settingsDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (315 * getResources().getDisplayMetrics().density);
            lp.height = (int) (210 * getResources().getDisplayMetrics().density);
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> settingsDialog.dismiss());
        }

        SeekBar volumeSeekBar = settingsDialog.findViewById(R.id.volumeSeekBar);
        if (volumeSeekBar != null) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                volumeSeekBar.setMax(maxVolume);
                volumeSeekBar.setProgress(currentVolume);

                volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
        }

        ImageButton exitButton = settingsDialog.findViewById(R.id.exitButton);
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                settingsDialog.dismiss();
                finishAffinity();
                // System.exit(0); // Avoid using this as it's not recommended
            });
        }

        settingsDialog.show();
    }

    private void setupTextContent() {
        TextView locationInfoText = findViewById(R.id.locationInfoText);

        if (building == null) {
            // Дефолтные данные, если информация о корпусе не загружена
            String mainText = "Полное название: Главный корпус КГУ\n\n" +
                    "Адрес: ул. Примерная, 1\n\n" +
                    "Про корпус: Это главный учебный корпус университета\n\n" +
                    "Что находится в корпусе: ректорат, главные аудитории, научные лаборатории\n\n" +
                    "Какие институты обучаются: Институт информатики, Институт строительства";

            locationInfoText.setText(mainText);
            return;
        }

        // Формируем текст с нужными заголовками
        StringBuilder mainText = new StringBuilder();

        // 1. Полное название
        mainText.append("Полное название: ")
                .append(building.getFullName() != null ? building.getFullName() : "Не указано")
                .append("\n\n");

        // 2. Адрес
        mainText.append("Адрес: ")
                .append(building.getAddress() != null ? building.getAddress() : "Не указан")
                .append("\n\n");

        // 3. Про корпус (история)
        mainText.append("Про корпус: ")
                .append(building.getHistory() != null ? building.getHistory() : "Нет информации")
                .append("\n\n");

        String title = building.getTitleHave();
        if (!title.isEmpty()) {
            // 4. Что находится в корпусе
            mainText.append(title).append(": ")
                    .append(building.getSubHave() != null ? building.getSubHave() : "Нет информации")
                    .append("\n\n");
        }

        // 5. Какие институты обучаются
        mainText.append("Какие институты обучаются: ");
        if (building.getInstitutes() != null && !building.getInstitutes().isEmpty()) {
            for (String institute : building.getInstitutes()) {
                mainText.append("\n- ").append(institute);
            }
        } else {
            mainText.append("Нет информации");
        }

        // 6. Связанные общежития (если есть)
        if (building.getRelatedDormitories() != null && !building.getRelatedDormitories().isEmpty()) {
            mainText.append("\n\n").append("В каких общежитиях проживают студенты:");
            for (String dor : building.getRelatedDormitories())
            {
                mainText.append("\n- ").append(dor);
            }
        }

        locationInfoText.setText(mainText.toString());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.stopMusic();
    }
}