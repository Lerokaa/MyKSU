package com.example.myksu;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DinoGameActivity extends AppCompatActivity {
    private GameView gameView;
    private TextView scoreText, gameOverText;
    private int score = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dino_game);

        // Инициализация UI элементов
        scoreText = findViewById(R.id.scoreText);
        gameOverText = findViewById(R.id.gameOverText);
        FrameLayout gameFrame = findViewById(R.id.gameFrame);
        // Button jumpButton = findViewById(R.id.jumpButton);
        ImageButton btnRestart = findViewById(R.id.btnRestart);
        ImageButton btnHelp = findViewById(R.id.btnHelp);
        ImageButton navButton = findViewById(R.id.navButton);

        // Создание игрового поля
        gameView = new GameView(this, new GameCallbacks() {
            @Override
            public void updateScore(int newScore) {
                score = newScore;
                runOnUiThread(() -> scoreText.setText("Счет: " + score));
            }

            @Override
            public void showAchievement(String message) {
                runOnUiThread(() -> Toast.makeText(
                        DinoGameActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show());
            }

            @Override
            public void gameOver() {
                runOnUiThread(() -> {
                    gameOverText.setText("Игра окончена!\nВаш счет: " + score +
                            "\nНажмите прыжок, чтобы начать заново");
                    gameOverText.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void hideGameOver() {
                runOnUiThread(() -> gameOverText.setVisibility(View.GONE));
            }
        });
        gameFrame.addView(gameView);


        btnHelp.setOnClickListener(v -> showHelpDialog());
        navButton.setOnClickListener(v -> finish());
    }

    private void showHelpDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Помощь")
                .setMessage("Нажимайте кнопку 'Прыжок', чтобы перепрыгивать через кактусы.\n\nЧем дольше играете, тем выше скорость!")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resumeGame();
    }

    interface GameCallbacks {
        void updateScore(int newScore);
        void gameOver();
        void hideGameOver();
        void showAchievement(String message);
    }
}
