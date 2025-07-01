package com.example.myksu;

import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class GuessNumberActivity extends AppCompatActivity {

    private EditText numberInput;
    private View checkButton;
    private Button newGameButton;
    private Button backToMapButton; // Новая кнопка для возврата на карту
    private int secretNumber;
    private int attemptsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_number);

        // Инициализация элементов
        numberInput = findViewById(R.id.number_input);
        checkButton = findViewById(R.id.check_button);
        newGameButton = findViewById(R.id.new_game_button);
        backToMapButton = findViewById(R.id.back_to_map_button); // Инициализация кнопки возврата

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        // Настройка кнопки "Назад"
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish()); // Закрывает текущую Activity и возвращает на предыдущую

        // Начало новой игры
        startNewGame();

        // Обработчик кнопки проверки
        checkButton.setOnClickListener(v -> checkGuess());

        // Обработчик кнопки новой игры
        newGameButton.setOnClickListener(v -> startNewGame());

        // Обработчик кнопки возврата на карту
        backToMapButton.setOnClickListener(v -> finish()); // Просто закрываем текущую активность
    }

    private void startNewGame() {
        // Генерация случайного числа
        Random random = new Random();
        secretNumber = random.nextInt(100) + 1;
        attemptsCount = 0;

        // Сброс UI
        numberInput.setText("");
        checkButton.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.GONE);
        backToMapButton.setVisibility(View.GONE); // Скрываем кнопку возврата
        numberInput.setEnabled(true);
        numberInput.requestFocus();
    }

    private void checkGuess() {
        String input = numberInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите число", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int guess = Integer.parseInt(input);

            if (guess < 1 || guess > 100) {
                Toast.makeText(this, "Число должно быть от 1 до 100", Toast.LENGTH_SHORT).show();
                return;
            }

            attemptsCount++;

            if (guess == secretNumber) {
                Toast.makeText(this, "Поздравляем! Вы угадали число за " + attemptsCount + " попыток!", Toast.LENGTH_LONG).show();
                checkButton.setVisibility(View.GONE);
                newGameButton.setVisibility(View.VISIBLE);
                backToMapButton.setVisibility(View.VISIBLE); // Показываем кнопку возврата
                numberInput.setEnabled(false);
            } else if (guess < secretNumber) {
                Toast.makeText(this, "Загаданное число больше", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Загаданное число меньше", Toast.LENGTH_SHORT).show();
            }

            numberInput.setText("");

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Пожалуйста, введите корректное число", Toast.LENGTH_SHORT).show();
        }
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