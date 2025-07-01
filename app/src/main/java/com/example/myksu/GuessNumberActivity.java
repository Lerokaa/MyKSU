package com.example.myksu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
}