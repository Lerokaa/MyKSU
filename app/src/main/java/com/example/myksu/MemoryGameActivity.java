package com.example.myksu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MemoryGameActivity extends AppCompatActivity {
    private MemoryGameView gameView;
    private TextView scoreText;
    private int attempts = 0;
    private int pairsFound = 0;
    private final int TOTAL_PAIRS = 8; // 4x4 grid = 16 cards = 8 pairs
    private int gridSize = 4; // 4x4 grid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memory_game);
        // Запуск музыки
        MusicManager.startMusic(this, R.raw.your_music);

        initializeViews();
        setupGame();

        ImageButton helpButton = findViewById(R.id.btnHelp);
        helpButton.setOnClickListener(v -> showHelpDialog());

        ImageButton backButton = findViewById(R.id.navButton);
        backButton.setOnClickListener(v -> finish());

        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        settingsButton.setOnClickListener(v -> showSettingsDialog());
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        scoreText.setText("Найдено пар: 0/" + TOTAL_PAIRS + "\nПопыток: 0");

        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        gameView = new MemoryGameView(this);
        gameContainer.addView(gameView);
    }

    private void setupGame() {
        attempts = 0;
        pairsFound = 0;
        gameView.resetGame();
        updateScore();
    }

    private void updateScore() {
        scoreText.setText("Найдено пар: " + pairsFound + "/" + TOTAL_PAIRS + "\nПопыток: " + attempts);
    }

    private void resetGame() {
        setupGame();
    }

    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(this);
        successDialog.setContentView(R.layout.success_dialog);

        successDialog.setTitle(null);
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Window window = successDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (300 * getResources().getDisplayMetrics().density);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ImageButton continueButton = successDialog.findViewById(R.id.dialog_continue);
        continueButton.setOnClickListener(v -> {
            successDialog.dismiss();
            Intent intent = new Intent(MemoryGameActivity.this, MapActivity.class);
            startActivity(intent);
            finish();
        });
        successDialog.show();
    }

    private void showHelpDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.memory_game_rule);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageButton closeButton = dialog.findViewById(R.id.dialog_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    class MemoryGameView extends View {
        private Paint paint;
        private int cardWidth, cardHeight;
        private List<Card> cards;
        private List<Card> flippedCards;
        private boolean isProcessing = false;
        private Random random = new Random();

        public MemoryGameView(Context context) {
            super(context);
            paint = new Paint();
            paint.setAntiAlias(true);
            cards = new ArrayList<>();
            flippedCards = new ArrayList<>();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            cardWidth = w / gridSize;
            cardHeight = h / gridSize;
            resetGame();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isProcessing || event.getAction() != MotionEvent.ACTION_DOWN) {
                return true;
            }

            int x = (int) event.getX();
            int y = (int) event.getY();

            int col = x / cardWidth;
            int row = y / cardHeight;

            if (col >= gridSize || row >= gridSize) {
                return true;
            }

            int index = row * gridSize + col;
            if (index >= 0 && index < cards.size()) {
                Card card = cards.get(index);
                if (!card.isFlipped && !card.isMatched) {
                    flipCard(card);
                }
            }

            return true;
        }

        private void flipCard(Card card) {
            card.isFlipped = true;
            flippedCards.add(card);
            invalidate();

            if (flippedCards.size() == 2) {
                attempts++;
                updateScore();
                isProcessing = true;

                new Handler().postDelayed(() -> {
                    checkForMatch();
                    isProcessing = false;
                }, 1000);
            }
        }

        private void checkForMatch() {
            if (flippedCards.size() != 2) return;

            Card card1 = flippedCards.get(0);
            Card card2 = flippedCards.get(1);

            if (card1.value == card2.value) {
                // Match found
                card1.isMatched = true;
                card2.isMatched = true;
                pairsFound++;
                updateScore();

                if (pairsFound == TOTAL_PAIRS) {
                    new Handler().postDelayed(() -> showSuccessDialog(), 500);
                }
            } else {
                // No match
                card1.isFlipped = false;
                card2.isFlipped = false;
            }

            flippedCards.clear();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.parseColor("#ffffff"));

            // Draw grid
            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth(2);
            for (int i = 0; i <= gridSize; i++) {
                canvas.drawLine(i * cardWidth, 0, i * cardWidth, getHeight(), paint);
                canvas.drawLine(0, i * cardHeight, getWidth(), i * cardHeight, paint);
            }

            // Draw cards
            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                int row = i / gridSize;
                int col = i % gridSize;
                int left = col * cardWidth;
                int top = row * cardHeight;
                int right = left + cardWidth;
                int bottom = top + cardHeight;

                if (card.isMatched) {
                    // Draw empty space for matched cards
                    paint.setColor(Color.parseColor("#5A7D8F"));
                    canvas.drawRect(left, top, right, bottom, paint);
                } else if (card.isFlipped) {
                    // Draw card face (color represents the value)
                    paint.setColor(card.color);
                    canvas.drawRect(left, top, right, bottom, paint);

                    // Draw card value (optional)
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(cardWidth / 2);
                    String text = String.valueOf(card.value);
                    float textWidth = paint.measureText(text);
                    canvas.drawText(text, left + (cardWidth - textWidth)/2,
                            top + cardHeight/2 + paint.getTextSize()/3, paint);
                } else {
                    // Draw card back
                    paint.setColor(Color.WHITE);
                    canvas.drawRect(left, top, right, bottom, paint);
                }

                // Draw card border
                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                canvas.drawRect(left, top, right, bottom, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }

        public void resetGame() {
            cards.clear();
            flippedCards.clear();
            isProcessing = false;

            // Create pairs of cards with random colors
            int[] colors = {
                    Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                    Color.CYAN, Color.MAGENTA, Color.parseColor("#FFA500"),
                    Color.parseColor("#800080")
            };

            // We need TOTAL_PAIRS pairs (2 cards each)
            for (int i = 0; i < TOTAL_PAIRS; i++) {
                int color = colors[i % colors.length];
                cards.add(new Card(i, color));
                cards.add(new Card(i, color));
            }

            // Shuffle the cards
            Collections.shuffle(cards);

            // Reset game state
            attempts = 0;
            pairsFound = 0;
            updateScore();
            invalidate();
        }

        class Card {
            int value;
            int color;
            boolean isFlipped = false;
            boolean isMatched = false;

            public Card(int value, int color) {
                this.value = value;
                this.color = color;
            }
        }
    }

    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);

        settingsDialog.setTitle(null);
        settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Window window = settingsDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (315 * getResources().getDisplayMetrics().density);
            lp.height = (int) (210 * getResources().getDisplayMetrics().density);
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ImageButton closeButton = settingsDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> settingsDialog.dismiss());

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

        ImageButton exitButton = settingsDialog.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            settingsDialog.dismiss();
            finishAffinity();
            System.exit(0);
        });

        settingsDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.resumeMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.stopMusic();
    }
}