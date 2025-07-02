package com.example.myksu;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class NextPieceView extends View {
    private static final int BLOCK_COLOR = Color.BLUE;
    private static final int[][][] SHAPES = {
            {{1, 1, 1, 1}}, // I
            {{1, 0, 0}, {1, 1, 1}}, // J
            {{0, 0, 1}, {1, 1, 1}}, // L
            {{1, 1}, {1, 1}}, // O
            {{0, 1, 1}, {1, 1, 0}}, // S
            {{0, 1, 0}, {1, 1, 1}}, // T
            {{1, 1, 0}, {0, 1, 1}}  // Z
    };

    private int[][] currentShape;
    private Paint paint;
    private int cellSize;
    private final RectF tempRect = new RectF();
    private final Random random = new Random();

    public NextPieceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        generateNewShape();
    }

    public int[][] generateNewShape() {
        currentShape = SHAPES[random.nextInt(SHAPES.length)];
        invalidate();
        return currentShape;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = Math.min(w / 4, h / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentShape == null) return;

        int centerX = (getWidth() - currentShape[0].length * cellSize) / 2;
        int centerY = (getHeight() - currentShape.length * cellSize) / 2;

        paint.setColor(BLOCK_COLOR);
        for (int i = 0; i < currentShape.length; i++) {
            for (int j = 0; j < currentShape[i].length; j++) {
                if (currentShape[i][j] == 1) {
                    tempRect.set(
                            centerX + j * cellSize,
                            centerY + i * cellSize,
                            centerX + (j + 1) * cellSize,
                            centerY + (i + 1) * cellSize
                    );
                    canvas.drawRect(tempRect, paint);

                    // Draw border
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(tempRect, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(BLOCK_COLOR);
                }
            }
        }
    }
}