package com.example.myksu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColorConnectionActivity extends AppCompatActivity {
    private GameView gameView;
    private TextView scoreText;
    private int score = 0;
    private int gridRows = 5, gridCols = 5;
    private Path currentPath;
    private List<Point> pathPoints = new ArrayList<>();
    private int cellWidth, cellHeight;
    private final int TARGET_SCORE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_color);

        initializeViews();
        setupGame();

        ImageButton helpButton = findViewById(R.id.btnHelp);
        helpButton.setOnClickListener(v -> showHelpDialog());

        ImageButton backButton = findViewById(R.id.navButton);
        backButton.setOnClickListener(v -> finish());

        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        ImageButton restartButton = findViewById(R.id.btnRestart);
        restartButton.setOnClickListener(v -> resetGame());
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        scoreText.setText("Соединено: 0/" + TARGET_SCORE);

        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        gameView = new GameView(this);
        gameContainer.addView(gameView);
    }

    private void setupGame() {
        score = 0;
        gameView.resetGame();
    }

    private void updateScore() {
        scoreText.setText("Соединено: " + score + "/" + TARGET_SCORE);
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
            Intent intent = new Intent(ColorConnectionActivity.this, MapActivity.class);
            startActivity(intent);
            finish(); // Закрываем текущую активность
        });
        successDialog.show();
    }

    private void showHelpDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.game_color_rule);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Находим кнопку закрытия и устанавливаем обработчик
        ImageButton closeButton = dialog.findViewById(R.id.dialog_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    class GameView extends View {
        private Paint paint;
        private List<Point> points;
        private Map<Integer, Path> paths;
        private Point selectedPoint = null;
        private Point currentTouchPoint = null;
        private int pointRadius = 80;
        private int[][] grid = new int[5][5]; // 5x5 grid

        public GameView(Context context) {
            super(context);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(18);
            paint.setStyle(Paint.Style.STROKE);
            points = new ArrayList<>();
            paths = new HashMap<>();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // Делаем квадратные ячейки
            int size = Math.min(w, h);
            cellWidth = size / gridCols;
            cellHeight = size / gridCols; // Используем gridCols для обеих размерностей
            resetGame();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            // Snap to grid
            x = snapToGrid(x, cellWidth);
            y = snapToGrid(y, cellHeight);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (Point point : points) {
                        if (isInPoint(x, y, point)) {
                            selectedPoint = point;
                            currentPath = new Path();
                            currentPath.moveTo(point.x, point.y);
                            pathPoints.clear();
                            pathPoints.add(point);
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (selectedPoint != null) {
                        // Only add point if it's different from last one
                        if (pathPoints.isEmpty() ||
                                x != pathPoints.get(pathPoints.size() - 1).x ||
                                y != pathPoints.get(pathPoints.size() - 1).y) {

                            Point newPoint = new Point(x, y, selectedPoint.color, -1);
                            pathPoints.add(newPoint);
                            currentPath.lineTo(x, y);
                            invalidate();
                        }
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (selectedPoint != null) {
                        for (Point point : points) {
                            if (isInPoint(x, y, point) && point.color == selectedPoint.color && point != selectedPoint) {
                                // Build path from recorded points
                                Path finalPath = new Path();
                                finalPath.moveTo(selectedPoint.x, selectedPoint.y);
                                for (android.graphics.Point p : pathPoints) {
                                    finalPath.lineTo(p.x, p.y);
                                }
                                finalPath.lineTo(point.x, point.y);

                                connectPoints(selectedPoint, point, finalPath);
                                break;
                            }
                        }
                        selectedPoint = null;
                        currentPath = null;
                        pathPoints.clear();
                        invalidate();
                    }
                    break;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.parseColor("#87CEEB"));

            // Draw grid
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(18);
            for (int i = 0; i <= gridCols; i++)
                canvas.drawLine(i * cellWidth, 0, i * cellWidth, getHeight(), paint);
            for (int j = 0; j <= gridRows; j++)
                canvas.drawLine(0, j * cellHeight, getWidth(), j * cellHeight, paint);

            // Draw connections
            for (Path path : new ArrayList<>(paths.values())) {
                Point start = findPointForPath(path);
                if (start != null) {
                    paint.setColor(start.color);
                    canvas.drawPath(path, paint);
                }
            }

            // Draw current path being drawn
            if (selectedPoint != null && currentPath != null) {
                paint.setColor(selectedPoint.color);
                canvas.drawPath(currentPath, paint);
            }

            // Draw points
            for (Point point : points) {
                paint.setColor(point.color);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(point.x, point.y, pointRadius, paint);

                paint.setStyle(Paint.Style.STROKE);

                canvas.drawCircle(point.x, point.y, pointRadius, paint);
            }
        }


        private boolean isInPoint(int x, int y, Point point) {
            return Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2)) <= pointRadius;
        }

        private void connectPoints(Point p1, Point p2, Path path) {
            if (paths.containsKey(p1.id)) return;

            if (!isPathCrossingExisting(p1, p2, path)) {
                paths.put(p1.id, path);
                paths.put(p2.id, path);
                score += 2;
                updateScore();

                if (score == TARGET_SCORE) {
                    postDelayed(() -> showSuccessDialog(), 500);
                }
            }
            invalidate();
        }

        private Path buildGridPath(Point p1, Point p2) {
            Path path = new Path();
            path.moveTo(p1.x, p1.y);

            // Get grid coordinates for both points
            int p1Col = (p1.x - cellWidth / 2) / cellWidth;
            int p1Row = (p1.y - cellHeight / 2) / cellHeight;
            int p2Col = (p2.x - cellWidth / 2) / cellWidth;
            int p2Row = (p2.y - cellHeight / 2) / cellHeight;

            // Current position in grid coordinates
            int currentCol = p1Col;
            int currentRow = p1Row;

            // Move horizontally first
            while (currentCol != p2Col) {
                if (currentCol < p2Col) currentCol++;
                else currentCol--;

                int x = currentCol * cellWidth + cellWidth / 2;
                int y = currentRow * cellHeight + cellHeight / 2;
                path.lineTo(x, y);
            }

            // Then move vertically
            while (currentRow != p2Row) {
                if (currentRow < p2Row) currentRow++;
                else currentRow--;

                int x = currentCol * cellWidth + cellWidth / 2;
                int y = currentRow * cellHeight + cellHeight / 2;
                path.lineTo(x, y);
            }

            return path;
        }

        private boolean isPathCrossingExisting(Point newP1, Point newP2, Path newPath) {
            // Get all grid cells the new path goes through
            Set<String> newPathCells = getPathCells(newPath);

            for (Map.Entry<Integer, Path> entry : paths.entrySet()) {
                Path existingPath = entry.getValue();
                Set<String> existingPathCells = getPathCells(existingPath);

                // Check for any common cells (excluding start/end points)
                for (String cell : newPathCells) {
                    if (existingPathCells.contains(cell) &&
                            !cell.equals(getCellKey(newP1.x, newP1.y)) &&
                            !cell.equals(getCellKey(newP2.x, newP2.y))) {
                        return true;
                    }
                }
            }
            return false;
        }

        private Set<String> getPathCells(Path path) {
            Set<String> cells = new HashSet<>();
            // This is a simplified approach - in a real implementation you'd need to
            // properly analyze the path segments
            PathMeasure pm = new PathMeasure(path, false);
            float[] coords = new float[2];
            for (float i = 0; i < pm.getLength(); i += cellWidth / 2) {
                pm.getPosTan(i, coords, null);
                cells.add(getCellKey((int) coords[0], (int) coords[1]));
            }
            return cells;
        }

        private String getCellKey(int x, int y) {
            int col = (x - cellWidth / 2) / cellWidth;
            int row = (y - cellHeight / 2) / cellHeight;
            return col + "," + row;
        }

        private boolean doPathsIntersect(Path path1, Path path2) {
            // Simplified intersection check (for grid-based paths)
            // In a real implementation, you would need proper path intersection logic
            return false;
        }

        private Point findPairedPoint(Point point) {
            for (Map.Entry<Integer, Path> entry : paths.entrySet()) {
                if (entry.getKey() != point.id && paths.get(entry.getKey()) == paths.get(point.id)) {
                    return points.get(entry.getKey());
                }
            }
            return null;
        }

        private Point findPointForPath(Path path) {
            for (Map.Entry<Integer, Path> entry : paths.entrySet()) {
                if (entry.getValue() == path) {
                    return points.get(entry.getKey());
                }
            }
            return null;
        }

        private int snapToGrid(int coord, int cellSize) {
            return (coord / cellSize) * cellSize + cellSize / 2;
        }

        public void resetGame() {
            points = new ArrayList<>();
            paths = new HashMap<>();
            score = 0;
            updateScore();

            // Initialize grid positions (1-25)
            // 1: (0,0), 5: (4,0), 8: (2,1), 11: (0,2), 14: (3,2), 16: (0,3), 19: (3,3), 20: (4,3), 21: (0,4), 25: (4,4)
            int[][] positions = {
                    {0, 0, Color.GREEN},    // 1 - зеленая
                    {4, 0, Color.YELLOW},   // 5 - желтая
                    {2, 1, Color.parseColor("#FFA500")}, // 8 - оранжевая
                    {0, 2, Color.GREEN},    // 11 - зеленая
                    {3, 2, Color.YELLOW},   // 14 - желтая
                    {0, 3, Color.CYAN},     // 16 - голубая
                    {3, 3, Color.CYAN},     // 19 - голубая
                    {4, 3, Color.parseColor("#FFA500")}, // 20 - оранжевая
                    {0, 4, Color.RED},      // 21 - красная
                    {4, 4, Color.RED}       // 25 - красная
            };

            for (int i = 0; i < positions.length; i++) {
                int col = positions[i][0];
                int row = positions[i][1];
                int color = positions[i][2];
                int x = col * cellWidth + cellWidth / 2;
                int y = row * cellHeight + cellHeight / 2;
                points.add(new Point(x, y, color, i));
            }
            invalidate();
        }

        class Point extends android.graphics.Point {
            int color;
            int id;

            public Point(int x, int y, int color, int id) {
                super(x, y);
                this.color = color;
                this.id = id;
            }
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