package com.example.myksu;

import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InformationAboutObshagi extends AppCompatActivity {

    private ViewPager2 photosCarousel;
    private CarouselAdapter carouselAdapter;
    private final Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private static final long AUTO_SCROLL_DELAY = 3000;
    private ImageView downArrow;
    private ScrollView scrollView;
    private DormitoryData.Dormitory dormitory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_about_obshagi);

        int dormitoryId = getIntent().getIntExtra("DORMITORY_ID", 1); // 1 - значение по умолчанию

        // Загружаем данные об общежитии
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.dormitories); // предполагается, что JSON лежит в res/raw
            dormitory = DormitoryData.parseSingleDormitory(inputStream, dormitoryId);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Инициализация элементов для стрелки
        downArrow = findViewById(R.id.downArrow);
        scrollView = findViewById(R.id.scrollView);

        initUI();
        setupTextContent();
        loadData();
        setupCarousel();
        setupButtonListeners();
        setupScrollListener();
    }

    private void setupScrollListener() {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!scrollView.canScrollVertically(1)) {
                // Если достигнут низ - скрыть стрелку
                downArrow.animate().alpha(0f).setDuration(300).withEndAction(() ->
                        downArrow.setVisibility(View.GONE)).start();
            } else {
                // Если можно скроллить вниз - показать стрелку
                downArrow.setVisibility(View.VISIBLE);
                downArrow.animate().alpha(1f).setDuration(300).start();
            }
        });

        // Показать стрелку через секунду после открытия (если нужно скроллить)
        downArrow.postDelayed(() -> {
            if (scrollView.canScrollVertically(1)) {
                downArrow.setVisibility(View.VISIBLE);
                downArrow.setAlpha(0f);
                downArrow.animate().alpha(0.7f).setDuration(500).start();
            }
        }, 1000);
    }

    // Остальные методы класса остаются без изменений
    private void initUI() {
        photosCarousel = findViewById(R.id.photosCarousel);
    }

    private void loadData() {
        if (dormitory == null) return;

        TextView addressText = findViewById(R.id.addressText);

        if (addressText != null) {
            // Формируем текст в формате: "Название\n(Адрес)"
            String addressString = dormitory.getName() + " (" + dormitory.getAddress() + ")";
            addressText.setText(addressString);
        }
    }

    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();

        // Если есть фото в данных об общежитии
        if (dormitory != null && dormitory.getPhotos() != null && !dormitory.getPhotos().isEmpty()) {
            for (String photoName : dormitory.getPhotos()) {
                try {
                    // Получаем идентификатор ресурса по имени файла (без расширения)
                    int resId = getResources().getIdentifier(
                            photoName.toLowerCase().replace(".jpg", "").replace(".png", ""),
                            "drawable",
                            getPackageName()
                    );

                    if (resId != 0) {
                        images.add(resId);
                    } else {
                        // Если изображение не найдено, можно добавить заглушку
                        images.add(R.drawable.other_photo1);
                        Log.e("Carousel", "Image not found: " + photoName);
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
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> showSettingsDialog());
        }

        ImageButton achievementsButton = findViewById(R.id.achievementsButton);
        if (achievementsButton != null) {
            achievementsButton.setOnClickListener(v -> {
                // Achievement button handler
            });
        }
    }

    private void setupTextContent() {
        if (dormitory == null) return;

        TextView contentText = findViewById(R.id.contentText);
        if (contentText == null) return;

        StringBuilder builder = new StringBuilder();
        boolean isFirstSection = true;

        // 1. Комендант
        isFirstSection = appendSection(builder, "Комендант общежития",
                dormitory.getCommandant(), "Не указан", isFirstSection);

        // 2. Телефон
        isFirstSection = appendSection(builder, "Номер телефона",
                dormitory.getPhone(), "Не указан", isFirstSection);

        // 3. Институты
        if (dormitory.getInstitutes() != null && !dormitory.getInstitutes().isEmpty()) {
            isFirstSection = appendSection(builder, "Проживают институты",
                    String.join(", ", dormitory.getInstitutes()), null, isFirstSection);
        }

        // 4. Описание здания
        isFirstSection = appendSection(builder, "Описание здания",
                dormitory.getBuilding(), "Не указано", isFirstSection);

        // 5. Инфраструктура
        if (dormitory.getInfrastructure() != null) {
            isFirstSection = appendSection(builder, "На первом этаже находится",
                    dormitory.getInfrastructure().getFirstFloor(), "Не указано", isFirstSection);

            String title = dormitory.getInfrastructure().getBlockStructureTitle();
            if (!title.isEmpty()) {
                isFirstSection = appendSection(builder, title,
                        dormitory.getInfrastructure().getBlockStructureDescription(), "Не указана", isFirstSection);
            }
        }

        // 6. Условия проживания
        if (dormitory.getLivingConditions() != null) {
            isFirstSection = appendSection(builder, "Как проживают студенты",
                    dormitory.getLivingConditions().getMessage(), "Не указано", isFirstSection);

            isFirstSection = appendSection(builder, "Что есть на каждом этаже",
                    dormitory.getLivingConditions().getPerFloor(), "Не указано", isFirstSection);
        }

        contentText.setText(builder.toString());
    }

    // Обновленный вспомогательный метод
    private boolean appendSection(StringBuilder builder, String title, String value,
                                  String defaultValue, boolean isFirstSection) {
        if (value == null || value.isEmpty()) {
            if (defaultValue == null) {
                return isFirstSection;
            }
            value = defaultValue;
        }

        if (!isFirstSection) {
            builder.append("\n\n");
        }

        builder.append(title).append(": ").append(value);
        return false;
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
        // Ваш существующий код для диалога настроек
    }
}