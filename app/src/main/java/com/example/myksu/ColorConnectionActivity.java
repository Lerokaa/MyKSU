package com.example.myksu;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ColorConnectionActivity extends AppCompatActivity {
    private ColorConnectionView gameView;
    private CardView rulesCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_connection);

        // Инициализация элементов
        gameView = findViewById(R.id.game_view);
        rulesCard = findViewById(R.id.rules_card);
        View helpButton = findViewById(R.id.help_button);
        View closeRulesButton = findViewById(R.id.close_rules_button);
        View backButton = findViewById(R.id.back_button);
        View resetButton = findViewById(R.id.reset_button);

        // Установка размеров игрового поля
        gameView.getLayoutParams().width = (int) (350 * getResources().getDisplayMetrics().density);
        gameView.getLayoutParams().height = (int) (350 * getResources().getDisplayMetrics().density);

        // Обработчики нажатий
        helpButton.setOnClickListener(v -> rulesCard.setVisibility(View.VISIBLE));
        closeRulesButton.setOnClickListener(v -> rulesCard.setVisibility(View.GONE));
        backButton.setOnClickListener(v -> finish());
        resetButton.setOnClickListener(v -> {
            gameView.resetGame();
            Toast.makeText(this, "Игра сброшена", Toast.LENGTH_SHORT).show();
        });

        // Установка слушателя завершения игры
        gameView.setGameCompleteListener(new ColorConnectionView.GameCompleteListener() {
            @Override
            public void onGameComplete() {
                Toast.makeText(ColorConnectionActivity.this,
                        "Поздравляем! Вы выиграли!", Toast.LENGTH_LONG).show();
                gameView.resetGame();
            }
        });

        // Показать правила при первом запуске
        showFirstTimeTips();
    }

    private void showFirstTimeTips() {
        rulesCard.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resetGame();
    }
}