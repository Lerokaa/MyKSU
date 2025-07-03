package com.example.myksu;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048Activity extends AppCompatActivity {
    private static final int GRID_SIZE = 4;
    private static final int TARGET_SCORE = 128;

    private GridLayout gameGrid;
    private TextView scoreText;
    private int score = 0;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private TextView[][] tileViews = new TextView[GRID_SIZE][GRID_SIZE];
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_2048);

        initializeViews();
        setupGame();
        setupGestureDetector();

        // Настройка кнопки помощи
        ImageButton helpButton = findViewById(R.id.btnHelp);
        helpButton.setOnClickListener(v -> showHelpDialog());

        // Настройка кнопки "Назад"
        ImageButton backButton = findViewById(R.id.navButton);
        backButton.setOnClickListener(v -> finish()); // Закрывает текущую Activity и возвращает на предыдущую

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        // Запуск музыки
        MusicManager.startMusic(this, R.raw.your_music);
    }

    private void initializeViews() {
        gameGrid = findViewById(R.id.gameGrid);
        scoreText = findViewById(R.id.scoreText);

        findViewById(R.id.navButton).setOnClickListener(v -> finish());
        findViewById(R.id.btnShuffle).setOnClickListener(v -> resetGame());
        findViewById(R.id.btnHelp).setOnClickListener(v -> showHelpDialog());
    }

    private void setupGame() {
        gameGrid.removeAllViews();
        gameGrid.setRowCount(GRID_SIZE);
        gameGrid.setColumnCount(GRID_SIZE);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
                tileViews[i][j] = null;
            }
        }

        score = 0;
        updateScore();

        addRandomTile();
        addRandomTile();

        updateGrid();
    }

    private void updateScore() {
        scoreText.setText("Счет: " + score);
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) moveRight();
                    else moveLeft();
                } else {
                    if (dy > 0) moveDown();
                    else moveUp();
                }
                return true;
            }
        });

        gameGrid.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void addRandomTile() {
        List<int[]> emptyCells = new ArrayList<>();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            Random random = new Random();
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            grid[cell[0]][cell[1]] = random.nextFloat() > 0.9f ? 4 : 2;
        }
    }

    private void updateGrid() {
        gameGrid.post(() -> {
            int gridWidth = gameGrid.getWidth();
            int gridHeight = gameGrid.getHeight();
            int size = Math.min(gridWidth, gridHeight);
            // Уменьшаем размер плиток (увеличиваем отступы)
            int tileSize = (size - (GRID_SIZE + 1) * 12) / GRID_SIZE; // Увеличили отступы с 4 до 12

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    TextView tile = tileViews[i][j];
                    if (tile == null) {
                        tile = new TextView(this);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = tileSize;
                        params.height = tileSize;
                        params.rowSpec = GridLayout.spec(i);
                        params.columnSpec = GridLayout.spec(j);
                        params.setMargins(6, 6, 6, 6); // Увеличили отступы
                        tile.setLayoutParams(params);

                        tile.setGravity(Gravity.CENTER);
                        tile.setTextSize(tileSize / 10); // Уменьшили размер текста
                        tile.setTextColor(Color.WHITE);
                        tile.setBackgroundResource(R.drawable.minigames_tile_background);

                        gameGrid.addView(tile);
                        tileViews[i][j] = tile;
                    }

                    if (grid[i][j] != 0) {
                        tile.setText(String.valueOf(grid[i][j]));
                        tile.setBackgroundColor(getTileColor(grid[i][j]));
                        // Уменьшаем размер текста еще больше для больших чисел
                        if (grid[i][j] > 100) {
                            tile.setTextSize(tileSize / 14);
                        } else if (grid[i][j] > 10) {
                            tile.setTextSize(tileSize / 12);
                        } else {
                            tile.setTextSize(tileSize / 10);
                        }
                    } else {
                        tile.setText("");
                        tile.setBackgroundColor(Color.parseColor("#CDC1B4"));
                    }
                }
            }
            updateScore();
        });
    }

    private int getTileColor(int value) {
        switch (value) {
            case 2:    return Color.parseColor("#3498DB");
            case 4:    return Color.parseColor("#2980B9");
            case 8:    return Color.parseColor("#1F618D");
            case 16:   return Color.parseColor("#154360");
            case 32:   return Color.parseColor("#5DADE2");
            case 64:   return Color.parseColor("#2E86C1");
            case 128:  return Color.parseColor("#2874A6");
            case 256:  return Color.parseColor("#21618C");
            case 512:  return Color.parseColor("#1B4F72");
            case 1024: return Color.parseColor("#0E4B8F");
            case 2048: return Color.parseColor("#0A3D62");
            default:   return Color.parseColor("#3C3A32");
        }
    }

    private void moveLeft() {
        boolean moved = false;
        int[][] oldGrid = copyGrid();

        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = grid[i].clone();
            int[] result = mergeTiles(row);

            if (!equals(row, result)) {
                System.arraycopy(result, 0, grid[i], 0, GRID_SIZE);
                moved = true;
            }
        }

        if (moved) {
            addRandomTile();
            updateGrid();
            checkGameState();
        }
    }

    private void moveRight() {
        boolean moved = false;
        int[][] oldGrid = copyGrid();

        for (int i = 0; i < GRID_SIZE; i++) {
            int[] originalRow = grid[i].clone();
            int[] row = reverseArray(originalRow);
            int[] result = mergeTiles(row);
            result = reverseArray(result);

            if (!equals(originalRow, result)) {
                System.arraycopy(result, 0, grid[i], 0, GRID_SIZE);
                moved = true;
            }
        }

        if (moved) {
            addRandomTile();
            updateGrid();
            checkGameState();
        }
    }

    private int[][] copyGrid() {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }

    private void moveUp() {
        boolean moved = false;

        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = new int[GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                column[i] = grid[i][j];
            }

            int[] result = mergeTiles(column);

            for (int i = 0; i < GRID_SIZE; i++) {
                if (grid[i][j] != result[i]) {
                    moved = true;
                }
                grid[i][j] = result[i];
            }
        }

        if (moved) {
            addRandomTile();
            updateGrid();
            checkGameState();
        }
    }

    private void moveDown() {
        boolean moved = false;

        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = new int[GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                column[i] = grid[i][j];
            }

            int[] reversed = reverseArray(column);
            int[] result = mergeTiles(reversed);
            result = reverseArray(result);

            for (int i = 0; i < GRID_SIZE; i++) {
                if (grid[i][j] != result[i]) {
                    moved = true;
                }
                grid[i][j] = result[i];
            }
        }

        if (moved) {
            addRandomTile();
            updateGrid();
            checkGameState();
        }
    }

    private int[] mergeTiles(int[] line) {
        int[] result = new int[GRID_SIZE];
        int position = 0;

        for (int i = 0; i < GRID_SIZE; i++) {
            if (line[i] != 0) {
                if (result[position] == 0) {
                    result[position] = line[i];
                } else if (result[position] == line[i]) {
                    result[position] *= 2;
                    score += result[position];
                    position++;
                } else {
                    position++;
                    result[position] = line[i];
                }
            }
        }
        return result;
    }

    private int[] reverseArray(int[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }

    private boolean equals(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private void checkGameState() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == TARGET_SCORE) {
                    showSuccessDialog();
                    return;
                }
            }
        }

        if (!hasAvailableMoves()) {
            showGameOverDialog();
        }
    }

    private boolean hasAvailableMoves() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 0) {
                    return true;
                }
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int current = grid[i][j];

                if (i < GRID_SIZE - 1 && current == grid[i + 1][j]) {
                    return true;
                }

                if (j < GRID_SIZE - 1 && current == grid[i][j + 1]) {
                    return true;
                }
            }
        }

        return false;
    }

    private void resetGame() {
        setupGame();
    }

    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(this);
        successDialog.setContentView(R.layout.success_dialog);

        // Убираем стандартный заголовок и делаем прозрачный фон
        successDialog.setTitle(null);
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Настраиваем размеры диалога и затемнение
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

        // Кнопка продолжения
        ImageButton continueButton = successDialog.findViewById(R.id.dialog_continue);
        continueButton.setOnClickListener(v -> {
            successDialog.dismiss();
            // Создаем Intent для перехода к MapActivity
            Intent intent = new Intent(Game2048Activity.this, MapActivity.class);
            startActivity(intent);
            finish(); // Закрываем текущую активность
        });
        successDialog.show();
    }

    private void showGameOverDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.success_dialog);
        dialog.setCancelable(false);

        TextView message = dialog.findViewById(R.id.dialog_message);
        message.setText("Игра окончена! Ваш счет: " + score);

        ImageButton continueButton = dialog.findViewById(R.id.dialog_continue);
        continueButton.setOnClickListener(v -> {
            dialog.dismiss();
            resetGame();
        });

        dialog.show();
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

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setContentView(R.layout.game_2048_rule);

        // Убираем стандартный заголовок и делаем прозрачный фон
        helpDialog.setTitle(null);
        helpDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Настраиваем размеры диалога и затемнение
        Window window = helpDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            // Устанавливаем размеры (например, 300x400 dp)
            lp.width = (int) (300 * getResources().getDisplayMetrics().density);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            // Устанавливаем уровень затемнения
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            // Включаем флаг затемнения
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        // Кнопка закрытия
        ImageButton closeButton = helpDialog.findViewById(R.id.dialog_close);
        closeButton.setOnClickListener(v -> helpDialog.dismiss());

        helpDialog.show();
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