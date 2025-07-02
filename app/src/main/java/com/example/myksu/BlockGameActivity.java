package com.example.myksu;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BlockGameActivity extends AppCompatActivity {
    private BlockGameView gameView;
    private NextPieceView nextPieceView;
    private TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_game);

        gameView = findViewById(R.id.gameView);
        nextPieceView = findViewById(R.id.nextPieceView);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.restartButton).setOnClickListener(v -> resetGame());
        findViewById(R.id.helpButton).setOnClickListener(v -> showHelp());
        findViewById(R.id.closeHelpButton).setOnClickListener(v -> hideHelp());

        nextPieceView.setOnClickListener(v -> {
            int[][] shape = nextPieceView.generateNewShape();
            gameView.addBlock(shape, 0, 0);
        });

        resetGame();
    }

    private void resetGame() {
        gameView.startNewGame();
        nextPieceView.generateNewShape();
        updateScore(0);
        Toast.makeText(this, "Игра начата заново", Toast.LENGTH_SHORT).show();
    }

    private void updateScore(int score) {
        scoreText.setText("Счёт: " + score);
    }

    private void showHelp() {
        findViewById(R.id.helpCard).setVisibility(View.VISIBLE);
    }

    private void hideHelp() {
        findViewById(R.id.helpCard).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetGame();
    }
}