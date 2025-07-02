package com.example.myksu;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.*;

public class ColorConnectionView extends View {
    private static final int GRID_SIZE = 5;
    private static final int LINE_WIDTH = 15;
    private static final int[] COLORS = {
            0xFFFF5252, 0xFF448AFF, 0xFF69F0AE, 0xFFFFD740,
            0xFFE040FB, 0xFF18FFFF, 0xFF76FF03, 0xFFFF6E40
    };

    private Paint paint, borderPaint, dotPaint;
    private Path userPath;
    private List<Point> currentLinePoints = new ArrayList<>();
    private float cellSize;
    private Point[][] grid;
    private Map<Integer, Point> points = new HashMap<>();
    private List<Connection> connections = new ArrayList<>();
    private Point selectedPoint;
    private boolean[][] occupiedCells;
    private int connectedPairs = 0;
    private GameCompleteListener gameCompleteListener;

    public interface GameCompleteListener {
        void onGameComplete();
    }

    public ColorConnectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);

        occupiedCells = new boolean[GRID_SIZE][GRID_SIZE];
        generateNewGame();
    }

    public void generateNewGame() {
        grid = new Point[GRID_SIZE][GRID_SIZE];
        connections.clear();
        currentLinePoints.clear();
        selectedPoint = null;
        connectedPairs = 0;

        for (boolean[] row : occupiedCells) {
            Arrays.fill(row, false);
        }

        Random random = new Random();
        for (int i = 0; i < COLORS.length / 2; i++) {
            int color = COLORS[i];

            // Первая точка
            int x1 = random.nextInt(GRID_SIZE);
            int y1 = random.nextInt(GRID_SIZE);
            while (grid[x1][y1] != null) {
                x1 = random.nextInt(GRID_SIZE);
                y1 = random.nextInt(GRID_SIZE);
            }
            grid[x1][y1] = new Point(x1, y1, color);

            // Вторая точка
            int x2 = random.nextInt(GRID_SIZE);
            int y2 = random.nextInt(GRID_SIZE);
            while ((x2 == x1 && y2 == y1) || grid[x2][y2] != null) {
                x2 = random.nextInt(GRID_SIZE);
                y2 = random.nextInt(GRID_SIZE);
            }
            grid[x2][y2] = new Point(x2, y2, color);
        }

        updatePointsMap();
        invalidate();
    }

    private void updatePointsMap() {
        points.clear();
        int id = 0;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (grid[x][y] != null) {
                    points.put(id++, grid[x][y]);
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = Math.min(w, h) / (float) GRID_SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        drawGrid(canvas);
        drawConnections(canvas);
        drawUserPath(canvas);
        drawPoints(canvas);
    }

    private void drawGrid(Canvas canvas) {
        for (int i = 0; i <= GRID_SIZE; i++) {
            float pos = i * cellSize;
            canvas.drawLine(pos, 0, pos, GRID_SIZE * cellSize, borderPaint);
            canvas.drawLine(0, pos, GRID_SIZE * cellSize, pos, borderPaint);
        }
    }

    private void drawConnections(Canvas canvas) {
        for (Connection conn : connections) {
            paint.setColor(conn.color);
            canvas.drawPath(conn.path, paint);
        }
    }

    private void drawUserPath(Canvas canvas) {
        if (userPath != null && selectedPoint != null) {
            paint.setColor(selectedPoint.color);
            canvas.drawPath(userPath, paint);
        }
    }

    private void drawPoints(Canvas canvas) {
        for (Point point : points.values()) {
            dotPaint.setColor(point.color);
            canvas.drawCircle(
                    point.x * cellSize + cellSize / 2,
                    point.y * cellSize + cellSize / 2,
                    cellSize / 6, dotPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        int gridX = (int) (x / cellSize);
        int gridY = (int) (y / cellSize);

        if (gridX < 0 || gridX >= GRID_SIZE || gridY < 0 || gridY >= GRID_SIZE) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(gridX, gridY);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(gridX, gridY, x, y);
                break;
            case MotionEvent.ACTION_UP:
                handleTouchUp(gridX, gridY);
                break;
        }

        invalidate();
        return true;
    }

    private void handleTouchDown(int gridX, int gridY) {
        if (grid[gridX][gridY] != null) {
            selectedPoint = grid[gridX][gridY];
            currentLinePoints.clear();
            currentLinePoints.add(new Point(gridX, gridY, selectedPoint.color));
            updateUserPath();
        }
    }

    private void handleTouchMove(int gridX, int gridY, float exactX, float exactY) {
        if (selectedPoint == null) return;

        Point lastPoint = currentLinePoints.get(currentLinePoints.size() - 1);
        if (lastPoint.x != gridX || lastPoint.y != gridY) {
            currentLinePoints.add(new Point(gridX, gridY, selectedPoint.color));
            updateUserPath(exactX, exactY);
        }
    }

    private void handleTouchUp(int gridX, int gridY) {
        if (selectedPoint == null || currentLinePoints.size() < 2) {
            resetCurrentLine();
            return;
        }

        if (grid[gridX][gridY] != null &&
                grid[gridX][gridY].color == selectedPoint.color &&
                grid[gridX][gridY] != selectedPoint) {

            if (isPathValid()) {
                createValidConnection();
                checkGameCompletion();
            }
        }

        resetCurrentLine();
    }

    private boolean isPathValid() {
        // Проверка пересечений с существующими соединениями
        for (Connection conn : connections) {
            if (pathsIntersect(conn)) {
                return false;
            }
        }

        // Проверка занятых клеток
        for (Point p : currentLinePoints) {
            if (occupiedCells[p.x][p.y] && !(p.x == selectedPoint.x && p.y == selectedPoint.y)) {
                return false;
            }
        }

        return true;
    }

    private boolean pathsIntersect(Connection existing) {
        for (int i = 1; i < currentLinePoints.size(); i++) {
            Point p1 = currentLinePoints.get(i-1);
            Point p2 = currentLinePoints.get(i);

            if (linesIntersect(p1.x, p1.y, p2.x, p2.y,
                    existing.startX, existing.startY, existing.endX, existing.endY)) {
                return true;
            }
        }
        return false;
    }

    private boolean linesIntersect(int x1, int y1, int x2, int y2,
                                   int x3, int y3, int x4, int y4) {
        int d = (x2 - x1) * (y4 - y3) - (y2 - y1) * (x4 - x3);
        if (d == 0) return false;

        float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / (float) d;
        float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / (float) d;

        return ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1;
    }

    private void createValidConnection() {
        Path path = new Path();
        path.moveTo(
                selectedPoint.x * cellSize + cellSize / 2,
                selectedPoint.y * cellSize + cellSize / 2
        );

        for (Point p : currentLinePoints) {
            path.lineTo(
                    p.x * cellSize + cellSize / 2,
                    p.y * cellSize + cellSize / 2
            );
        }

        Point lastPoint = currentLinePoints.get(currentLinePoints.size()-1);
        Connection conn = new Connection(
                path, selectedPoint.color,
                selectedPoint.x, selectedPoint.y,
                lastPoint.x, lastPoint.y
        );

        connections.add(conn);
        markOccupiedCells(conn);
        connectedPairs++;
    }

    private void markOccupiedCells(Connection conn) {
        int steps = Math.max(
                Math.abs(conn.endX - conn.startX),
                Math.abs(conn.endY - conn.startY)
        );

        for (int i = 0; i <= steps; i++) {
            int x = conn.startX + (conn.endX - conn.startX) * i / steps;
            int y = conn.startY + (conn.endY - conn.startY) * i / steps;
            if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
                occupiedCells[x][y] = true;
            }
        }
    }

    private void checkGameCompletion() {
        if (connectedPairs == COLORS.length / 2 && gameCompleteListener != null) {
            gameCompleteListener.onGameComplete();
        }
    }

    public void resetGame() {
        connections.clear();
        resetCurrentLine();
        connectedPairs = 0;
        for (boolean[] row : occupiedCells) {
            Arrays.fill(row, false);
        }
        invalidate();
    }

    public void newGame() {
        connections.clear();
        resetCurrentLine();
        connectedPairs = 0;
        for (boolean[] row : occupiedCells) {
            Arrays.fill(row, false);
        }
        generateNewGame();
    }

    public void setGameCompleteListener(GameCompleteListener listener) {
        this.gameCompleteListener = listener;
    }

    private void resetCurrentLine() {
        userPath = null;
        currentLinePoints.clear();
        selectedPoint = null;
    }

    private void updateUserPath() {
        updateUserPath(
                currentLinePoints.get(currentLinePoints.size()-1).x * cellSize + cellSize / 2,
                currentLinePoints.get(currentLinePoints.size()-1).y * cellSize + cellSize / 2
        );
    }

    private void updateUserPath(float endX, float endY) {
        userPath = new Path();
        userPath.moveTo(
                selectedPoint.x * cellSize + cellSize / 2,
                selectedPoint.y * cellSize + cellSize / 2
        );

        for (Point p : currentLinePoints) {
            userPath.lineTo(
                    p.x * cellSize + cellSize / 2,
                    p.y * cellSize + cellSize / 2
            );
        }

        userPath.lineTo(endX, endY);
    }

    private static class Point {
        final int x, y, color;

        Point(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private static class Connection {
        final Path path;
        final int color;
        final int startX, startY, endX, endY;

        Connection(Path path, int color, int startX, int startY, int endX, int endY) {
            this.path = path;
            this.color = color;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }
    }
}