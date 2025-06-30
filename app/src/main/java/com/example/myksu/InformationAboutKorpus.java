package com.example.myksu;

import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class InformationAboutKorpus extends AppCompatActivity {

    private ViewPager2 photosCarousel;
    private CarouselAdapter carouselAdapter;
    private final Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private static final long AUTO_SCROLL_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_about_korpus);

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
        if (addressText != null) {  // Add null check for safety
            addressText.setText("Главный корпус КГУ");
        }
    }

    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.other_photo1);
        images.add(R.drawable.other_photo2);
        images.add(R.drawable.other_photo3);

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
                // Обработчик кнопки "Достижения"
            });
        }

        ImageButton startButton = findViewById(R.id.startButton);
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                // Действие при нажатии "Начать"
            });
        }

        ImageButton continueButton = findViewById(R.id.continueButton);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                // Действие при нажатии "Продолжить"
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
        TextView historyInfoText = findViewById(R.id.historyInfoText);

        // Форматированный текст с переносами строк
        String mainText = "Это мега супер крутой главный корпус!\n\n" +
                "Он такой единственный и важный, вся Кострома от него строилась.\n\n" +
                "Историческая справка: построен в 1965 году, является памятником архитектуры.\n\n" +
                "Здесь находятся: ректорат, главные аудитории, научные лаборатории.";

        locationInfoText.setText(mainText);
        historyInfoText.setText("Общежитие №1\n(1 переулок Воскресенский, 17)");
    }
}