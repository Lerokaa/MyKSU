package com.example.myksu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class BlockGameView extends View {
    private static final int GRID_SIZE = 8;
    private static final int BLOCK_COLOR = Color.BLUE;
    private static final String TAG = "BlockGameView";

    private Paint paint;
    private int cellSize;
    private final List<Block> blocks = new ArrayList<>();
    private Block draggedBlock;
    private float dragOffsetX, dragOffsetY;
    private final RectF tempRect = new RectF();
    private final Path gridPath = new Path();
    private int score = 0;

    public BlockGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        setClickable(true);
    }

    public void startNewGame() {
        blocks.clear();
        score = 0;
        invalidate();
    }

    public void addBlock(int[][] shape, int gridX, int gridY) {
        blocks.add(new Block(shape, gridX * cellSize, gridY * cellSize));
        checkCompletedLines();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = Math.min(w, h) / GRID_SIZE;
        prepareGridPath();
    }

    private void prepareGridPath() {
        gridPath.reset();
        for (int i = 0; i <= GRID_SIZE; i++) {
            gridPath.moveTo(i * cellSize, 0);
            gridPath.lineTo(i * cellSize, GRID_SIZE * cellSize);
            gridPath.moveTo(0, i * cellSize);
            gridPath.lineTo(GRID_SIZE * cellSize, i * cellSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        // Рисуем сетку
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(gridPath, paint);

        // Рисуем блоки
        paint.setStyle(Paint.Style.FILL);
        for (Block block : blocks) {
            drawBlock(canvas, block);
        }

        // Рисуем перетаскиваемый блок поверх остальных
        if (draggedBlock != null) {
            drawBlock(canvas, draggedBlock);
        }
    }

    private void drawBlock(Canvas canvas, Block block) {
        paint.setColor(BLOCK_COLOR);
        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[i].length; j++) {
                if (block.shape[i][j] == 1) {
                    tempRect.set(
                            block.x + j * cellSize,
                            block.y + i * cellSize,
                            block.x + (j + 1) * cellSize,
                            block.y + (i + 1) * cellSize
                    );
                    canvas.drawRect(tempRect, paint);

                    // Граница блока
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(tempRect, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(BLOCK_COLOR);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = blocks.size() - 1; i >= 0; i--) {
                    Block block = blocks.get(i);
                    if (isPointInBlock(x, y, block)) {
                        draggedBlock = block;
                        dragOffsetX = x - block.x;
                        dragOffsetY = y - block.y;
                        blocks.remove(i);
                        invalidate();
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draggedBlock != null) {
                    draggedBlock.x = x - dragOffsetX;
                    draggedBlock.y = y - dragOffsetY;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (draggedBlock != null) {
                    snapToGrid(draggedBlock);
                    blocks.add(draggedBlock);
                    draggedBlock = null;
                    checkCompletedLines(); // Проверяем линии после размещения блока
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void checkCompletedLines() {
        // Создаем матрицу для отслеживания заполненных клеток
        boolean[][] gridCells = new boolean[GRID_SIZE][GRID_SIZE];

        // Заполняем матрицу на основе текущих блоков
        for (Block block : blocks) {
            int gridX = (int)(block.x / cellSize);
            int gridY = (int)(block.y / cellSize);

            for (int i = 0; i < block.shape.length; i++) {
                for (int j = 0; j < block.shape[i].length; j++) {
                    if (block.shape[i][j] == 1) {
                        int x = gridX + j;
                        int y = gridY + i;
                        if (x < GRID_SIZE && y < GRID_SIZE) {
                            gridCells[y][x] = true;
                        }
                    }
                }
            }
        }

        // Проверяем заполненные строки
        List<Integer> completedRows = new ArrayList<>();
        for (int y = 0; y < GRID_SIZE; y++) {
            boolean fullRow = true;
            for (int x = 0; x < GRID_SIZE; x++) {
                if (!gridCells[y][x]) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) {
                completedRows.add(y);
            }
        }

        // Проверяем заполненные столбцы
        List<Integer> completedCols = new ArrayList<>();
        for (int x = 0; x < GRID_SIZE; x++) {
            boolean fullCol = true;
            for (int y = 0; y < GRID_SIZE; y++) {
                if (!gridCells[y][x]) {
                    fullCol = false;
                    break;
                }
            }
            if (fullCol) {
                completedCols.add(x);
            }
        }

        // Удаляем заполненные линии
        if (!completedRows.isEmpty() || !completedCols.isEmpty()) {
            removeCompletedLines(completedRows, completedCols);
            score += (completedRows.size() + completedCols.size()) * 10;
            Log.d(TAG, "Score: " + score);
        }
    }

    private void removeCompletedLines(List<Integer> rows, List<Integer> cols) {
        List<Block> newBlocks = new ArrayList<>();

        for (Block block : blocks) {
            int gridX = (int)(block.x / cellSize);
            int gridY = (int)(block.y / cellSize);
            boolean keepBlock = false;

            // Проверяем, пересекается ли блок с удаляемыми линиями
            blockLoop:
            for (int i = 0; i < block.shape.length; i++) {
                for (int j = 0; j < block.shape[i].length; j++) {
                    if (block.shape[i][j] == 1) {
                        int y = gridY + i;
                        int x = gridX + j;

                        // Если блок не пересекается с удаляемыми линиями, оставляем его
                        if (!rows.contains(y) && !cols.contains(x)) {
                            keepBlock = true;
                            break blockLoop;
                        }
                    }
                }
            }

            if (keepBlock) {
                // Создаем новый блок только с теми частями, которые не в удаляемых линиях
                int[][] newShape = new int[block.shape.length][block.shape[0].length];
                for (int i = 0; i < block.shape.length; i++) {
                    for (int j = 0; j < block.shape[i].length; j++) {
                        if (block.shape[i][j] == 1) {
                            int y = gridY + i;
                            int x = gridX + j;
                            if (!rows.contains(y) && !cols.contains(x)) {
                                newShape[i][j] = 1;
                            }
                        }
                    }
                }
                newBlocks.add(new Block(newShape, block.x, block.y));
            }
        }

        blocks.clear();
        blocks.addAll(newBlocks);
    }

    private boolean isPointInBlock(float x, float y, Block block) {
        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[i].length; j++) {
                if (block.shape[i][j] == 1) {
                    tempRect.set(
                            block.x + j * cellSize,
                            block.y + i * cellSize,
                            block.x + (j + 1) * cellSize,
                            block.y + (i + 1) * cellSize
                    );
                    if (tempRect.contains(x, y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void snapToGrid(Block block) {
        int gridX = Math.round(block.x / cellSize);
        int gridY = Math.round(block.y / cellSize);

        gridX = Math.max(0, Math.min(gridX, GRID_SIZE - block.shape[0].length));
        gridY = Math.max(0, Math.min(gridY, GRID_SIZE - block.shape.length));

        block.x = gridX * cellSize;
        block.y = gridY * cellSize;
    }

    private static class Block {
        final int[][] shape;
        float x, y;

        Block(int[][] shape, float x, float y) {
            this.shape = shape;
            this.x = x;
            this.y = y;
        }
    }
}