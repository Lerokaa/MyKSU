package com.example.myksu;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CharactersDialogActivity extends AppCompatActivity {

    private ViewPager2 photosCarousel;
    private CarouselAdapter carouselAdapter;
    private final Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private int currentPage = 0;
    private static final long AUTO_SCROLL_DELAY = 3000; // 3 секунды
    private DialoguesData.Dialog currentDialog;
    private TextView characterText;
    private List<String> phrases;
    private int currentPhraseIndex = 0;
    private BuildingData.Building currentBuilding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.characters_dialog);

        // Получаем ID из Intent
        int id = getIntent().getIntExtra("DIALOG_ID", -1);
        if (id == -1) {
            showErrorAndFinish("Ошибка: не передан ID корпуса");
            return;
        }

        // Загружаем данные с подробным логированием
        try {
            // Загрузка диалога
            currentDialog = loadDialogData(id);
            if (currentDialog == null) {
                showErrorAndFinish("Диалог для корпуса " + id + " не найден");
                return;
            }

            // Загрузка данных здания
            currentBuilding = loadBuildingData(id);

            // Инициализация UI
            initUI();
            setupUIComponents();
            setupCarousel();
            setupButtonListeners();

        } catch (Exception e) {
            Log.e("DATA_LOAD", "Критическая ошибка загрузки", e);
            showErrorAndFinish("Ошибка загрузки данных. Попробуйте позже.");
        }
    }

    private DialoguesData.Dialog loadDialogData(int id) {
        try (InputStream is = getResources().openRawResource(R.raw.dialogues)) {
            DialoguesData.Dialog dialog = DialoguesData.parseSingleDialog(is, id);
            if (dialog == null) {
                Log.e("DIALOG_LOAD", "Диалог с ID " + id + " не найден в dialogues.json");
            }
            return dialog;
        } catch (Exception e) {
            Log.e("DIALOG_LOAD", "Ошибка чтения dialogues.json", e);
            return null;
        }
    }

    private BuildingData.Building loadBuildingData(int id) {
        try (InputStream is = getResources().openRawResource(R.raw.buildings)) {
            BuildingData.Building building = BuildingData.parseSingleBuilding(is, id);
            if (building == null) {
                Log.w("BUILDING_LOAD", "Здание с ID " + id + " не найдено в buildings.json");
            }
            return building;
        } catch (Exception e) {
            Log.e("BUILDING_LOAD", "Ошибка чтения buildings.json", e);
            return null;
        }
    }

    private void showErrorAndFinish(String message) {
        Log.e("NONO", message);
        finish();
    }

    private void initUI() {
        photosCarousel = findViewById(R.id.photosCarousel);
    }

    private void loadDialogData() {
        if (currentDialog == null) return;

        TextView addressText = findViewById(R.id.addressText);
        addressText.setText(currentBuilding.getShortName());

        ImageView characterImage = findViewById(R.id.characterImage);
        String imageName = currentDialog.getPic().get(0); // Берем первую картинку как основную
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        characterImage.setImageResource(resId != 0 ? resId : R.drawable.mtr_k_gl_1);

        TextView characterName = findViewById(R.id.characterName);
        characterName.setText(currentDialog.getCharacter());

        characterText = findViewById(R.id.characterText);
        phrases = currentDialog.getPhrases();

        // Показываем первую фразу
        if (!phrases.isEmpty()) {
            characterText.setText(phrases.get(0));
            currentPhraseIndex = 0;
        }

        // Настраиваем обработчик кликов
        characterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextPhrase();
            }
        });
    }

    private void setupUIComponents() {
        // Установка адреса
        TextView addressText = findViewById(R.id.addressText);
        addressText.setText(currentBuilding != null ? currentBuilding.getShortName() : "Адрес не указан");

        // Загрузка изображения персонажа
        ImageView characterImage = findViewById(R.id.characterImage);
        try {
            String imageName = currentDialog.getPic().get(0);

            // Удаляем расширение .png если оно есть
            String resourceName = imageName.replace(".png", "").replace(".jpg", "").replace(".webp", "");

            // Получаем ID ресурса
            int resId = getResources().getIdentifier(
                    resourceName,  // имя без расширения
                    "drawable",    // тип ресурса
                    getPackageName()
            );

            // Логирование для отладки
            Log.d("CHARACTER_IMAGE", "Trying to load: " + resourceName + " (original: " + imageName + ")");
            Log.d("CHARACTER_IMAGE", "Resource ID: " + resId);

            if (resId == 0) {
                throw new Resources.NotFoundException("Изображение " + resourceName + " не найдено");
            }
            characterImage.setImageResource(resId);
        } catch (Exception e) {
            Log.e("CHARACTER_IMAGE", "Ошибка загрузки изображения персонажа", e);
            characterImage.setImageResource(R.drawable.mtr_k_gl_1); // fallback
        }

        // Установка имени персонажа
        TextView characterName = findViewById(R.id.characterName);
        characterName.setText(currentDialog.getCharacter());

        // Настройка фраз
        setupPhrases();
    }

    private void setupPhrases() {
        characterText = findViewById(R.id.characterText);
        phrases = currentDialog.getPhrases();

        if (phrases == null || phrases.isEmpty()) {
            Log.e("PHRASES", "No phrases available");
            characterText.setText("Диалог недоступен");
            characterText.setClickable(false);
            return;
        }

        // Отображаем первую фразу
        currentPhraseIndex = 0;
        updatePhraseDisplay();

        // Настраиваем обработчик кликов
        characterText.setOnClickListener(v -> showNextPhrase());
    }

    private void updatePhraseDisplay() {
        if (phrases == null || currentPhraseIndex >= phrases.size()) return;

        String currentPhrase = phrases.get(currentPhraseIndex);
        characterText.setText(currentPhrase);

        // Анимация смены текста
        characterText.setAlpha(0.1f);
        characterText.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void showNextPhrase() {
        if (phrases == null || phrases.isEmpty()) return;

        currentPhraseIndex++;

        if (currentPhraseIndex >= phrases.size()) {
            // Диалог завершен
            handleDialogComplete();
            return;
        }

        updatePhraseDisplay();
    }

    private void handleDialogComplete() {
        // Можно добавить обработку завершения диалога
        characterText.setText("Диалог завершён");
        characterText.setClickable(false);

        // Или автоматический переход
        // goToNextActivity();
    }

    private void goToNextActivity() {
//        // Пример перехода на следующую активити
//        Intent intent = new Intent(this, NextActivity.class);
//
//        // Можно передать дополнительные данные, если нужно
//        intent.putExtra("SOME_KEY", currentDialog.getId());
//
//        startActivity(intent);
//        finish(); // Закрываем текущую активити
    }

    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();

        // 1. Добавляем логирование для отладки
        Log.d("PHOTOS_DEBUG", "Building photos: " + (currentBuilding != null ? currentBuilding.getPhotos() : "null"));

        if (currentBuilding != null && currentBuilding.getPhotos() != null) {
            for (String photoName : currentBuilding.getPhotos()) {
                // 2. Полное имя ресурса для отладки
                String fullResourceName = getPackageName() + ":drawable/" + photoName;
                Log.d("PHOTOS_DEBUG", "Trying to load: " + fullResourceName);

                // 3. Альтернативные варианты поиска ресурса
                int resId = getResources().getIdentifier(
                        photoName.toLowerCase(),  // пробуем lowercase
                        "drawable",
                        getPackageName()
                );

                if (resId == 0) {
                    // Пробуем без расширения, если имя содержит .jpg/.png
                    String cleanName = photoName.replace(".jpg", "").replace(".png", "");
                    resId = getResources().getIdentifier(cleanName, "drawable", getPackageName());
                }

                if (resId != 0) {
                    images.add(resId);
                    Log.d("PHOTOS_DEBUG", "Successfully added: " + photoName);
                } else {
                    Log.e("PHOTOS_ERROR", "Resource not found: " + photoName);
                }
            }
        }

        // 4. Проверка результатов перед созданием адаптера
        if (images.isEmpty()) {
            Log.w("PHOTOS_WARN", "No images found, adding fallback");
            images.add(R.drawable.other_photo1);
        }

        // 5. Логирование итогового списка изображений
        Log.d("PHOTOS_DEBUG", "Final images list size: " + images.size());

        // Настраиваем адаптер карусели
        carouselAdapter = new CarouselAdapter(this, images);
        photosCarousel.setAdapter(carouselAdapter);

        // Устанавливаем начальную позицию в середину "виртуального" списка
        int startPosition = carouselAdapter.getItemCount() / 2;
        photosCarousel.setCurrentItem(startPosition, false);

        // Настройка автоматической прокрутки
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = photosCarousel.getCurrentItem() + 1;
                photosCarousel.setCurrentItem(nextItem, true);
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };

        // Обработчик ручного переключения страниц
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
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int itemCount = carouselAdapter.getItemCount();
                    int currentItem = photosCarousel.getCurrentItem();

                    // Бесконечная прокрутка - при достижении краев переходим к середине
                    if (currentItem == 0) {
                        photosCarousel.setCurrentItem(itemCount / 2, false);
                    } else if (currentItem == itemCount - 1) {
                        photosCarousel.setCurrentItem(itemCount / 2, false);
                    }
                }
            }
        });
    }

    private void setupButtonListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CharactersDialogActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        ImageButton achievementsButton = findViewById(R.id.achievementsButton);
        achievementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработчик кнопки "Достижения"
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);

        // Убираем стандартный заголовок и делаем прозрачный фон
        settingsDialog.setTitle(null);
        settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Настраиваем размеры диалога и затемнение
        Window window = settingsDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            // Устанавливаем фиксированные размеры (315x210 dp)
            lp.width = (int) (315 * getResources().getDisplayMetrics().density);
            lp.height = (int) (210 * getResources().getDisplayMetrics().density);
            // Устанавливаем уровень затемнения (0.7f - 70% затемнения)
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            // Включаем флаг затемнения
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        // Кнопка закрытия
        ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> settingsDialog.dismiss());

        // Настройка SeekBar для громкости
        SeekBar volumeSeekBar = settingsDialog.findViewById(R.id.volumeSeekBar);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

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

        // Получаем кнопку выхода
        ImageButton exitButton = settingsDialog.findViewById(R.id.exitButton);

        // Обработчик клика для выхода из приложения
        View.OnClickListener exitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Закрываем диалог
                settingsDialog.dismiss();

                // Полностью закрываем приложение
                finishAffinity(); // Закрывает все Activity
                System.exit(0);   // Завершает процесс
            }
        };

        // Назначаем обработчик на кнопку
        exitButton.setOnClickListener(exitListener);

        settingsDialog.show();
    }
}