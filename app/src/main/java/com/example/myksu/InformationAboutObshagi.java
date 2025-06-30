package com.example.myksu;

import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_about_obshagi);

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
        TextView addressText = findViewById(R.id.addressText);
        if (addressText != null) {
            addressText.setText("Общежитие №1");
        }
    }

    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.other_photo1);
        images.add(R.drawable.other_photo2);
        images.add(R.drawable.other_photo3);

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
        TextView contentText = findViewById(R.id.contentText);

        String fullText = "Это мега супер крутой главный корпус!\n\n" +
                "Он такой единственный и важный, вся Кострома от него строилась.\n\n" +
                "Историческая справка: построен в 1965 году, является памятником архитектуры.\n\n" +
                "Здесь находятся: ректорат, главные аудитории, научные лаборатории.";

        contentText.setText(fullText);
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