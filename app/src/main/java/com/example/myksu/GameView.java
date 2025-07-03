package com.example.myksu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private static final int GROUND_LEVEL = 80;
    private static final int DINO_WIDTH = 80;
    private static final int DINO_HEIGHT = 100;
    private static final int OBSTACLE_WIDTH = 60;
    private static final int OBSTACLE_HEIGHT = 80;

    private Paint paint;
    private int dinoY, dinoX;
    private boolean isJumping = false;
    private int jumpVelocity = 0;
    private Handler handler;
    private Runnable gameLoop;
    private List<Obstacle> obstacles = new ArrayList<>();
    private Random random = new Random();
    private int score = 0;
    private int gameSpeed = 10;
    private boolean isGameRunning = true;
    private DinoGameActivity.GameCallbacks callbacks;
    private int screenWidth = 0;

    // Анимация динозавра
    private Bitmap dinoBitmap1, dinoBitmap2;
    private boolean currentDinoBitmap = false;
    private int animationCounter = 0;

    public GameView(Context context, DinoGameActivity.GameCallbacks callbacks) {
        super(context);
        init(context, callbacks);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    private void init(Context context, DinoGameActivity.GameCallbacks callbacks) {
        this.callbacks = callbacks;
        paint = new Paint();

        dinoBitmap1 = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.dino1),
                DINO_WIDTH, DINO_HEIGHT, false);
        dinoBitmap2 = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.dino1),
                DINO_WIDTH, DINO_HEIGHT, false);

        handler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    update();
                    invalidate();
                    handler.postDelayed(this, 30);
                }
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        dinoX = 100;
        dinoY = h - DINO_HEIGHT - GROUND_LEVEL;
        resetGame();
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            jumpVelocity = -25;
        }
    }

    private void update() {
        // Обновление позиции динозавра
        if (isJumping) {
            dinoY += jumpVelocity;
            jumpVelocity += 2;

            if (dinoY >= getHeight() - DINO_HEIGHT - GROUND_LEVEL) {
                dinoY = getHeight() - DINO_HEIGHT - GROUND_LEVEL;
                isJumping = false;
            }
        }

        // Генерация препятствий
        generateObstacles();

        // Движение и проверка препятствий
        moveAndCheckObstacles();
    }

    private void generateObstacles() {
        if (obstacles.isEmpty()) {
            // Первое препятствие
            createNewObstacle(screenWidth);
        } else {
            // Последнее препятствие
            Obstacle lastObstacle = obstacles.get(obstacles.size() - 1);

            // Создаем новое, когда предыдущее прошло 2/3 экрана
            if (lastObstacle.x < screenWidth * 2 / 3 && obstacles.size() < 3) {
                createNewObstacle(screenWidth + random.nextInt(200));
            }
        }
    }

    private void createNewObstacle(int xPos) {
        int obstacleType = random.nextInt(3);
        int height = OBSTACLE_HEIGHT;

        switch (obstacleType) {
            case 0: height = OBSTACLE_HEIGHT / 3; break;
            case 1: height = OBSTACLE_HEIGHT * 2 / 3; break;
            case 2: height = OBSTACLE_HEIGHT; break;
        }

        Obstacle obstacle = new Obstacle(
                xPos,
                getHeight() - GROUND_LEVEL - height,
                OBSTACLE_WIDTH,
                height
        );

        obstacles.add(obstacle);
    }

    private void moveAndCheckObstacles() {
        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obstacle = iterator.next();
            obstacle.x -= gameSpeed;

            // Проверка столкновения
            if (checkCollision(obstacle)) {
                gameOver();
                return;
            }

            // Удаление за экраном
            if (obstacle.x < -obstacle.width) {
                iterator.remove();
                increaseScore();
            }
        }
    }

    private boolean checkCollision(Obstacle obstacle) {
        return Rect.intersects(
                new Rect(dinoX, dinoY, dinoX + DINO_WIDTH, dinoY + DINO_HEIGHT),
                new Rect(obstacle.x, obstacle.y, obstacle.x + obstacle.width, obstacle.y + obstacle.height)
        );
    }

    private void increaseScore() {
        score++;

        // Увеличиваем скорость каждые 10 очков
        if (score % 10 == 0) {
            gameSpeed = Math.min(25, gameSpeed + 1);
        }

        // Показываем достижение при 15 очках
        if (score == 15) {
            showAchievementToast();
        }

        if (callbacks != null) {
            callbacks.updateScore(score);
        }
    }

    private void showAchievementToast() {
        if (callbacks != null) {
            callbacks.showAchievement("Поздравляем! Вы получили достижение!");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Отрисовка фона (земли)
        paint.setColor(Color.parseColor("#F0F0F0"));
        canvas.drawRect(0, getHeight() - GROUND_LEVEL, getWidth(), getHeight(), paint);

        // Отрисовка динозавра
        Bitmap currentDino = currentDinoBitmap ? dinoBitmap2 : dinoBitmap1;
        canvas.drawBitmap(currentDino, dinoX, dinoY, paint);

        // Отрисовка препятствий
        paint.setColor(Color.parseColor("#555555"));
        for (Obstacle obstacle : obstacles) {
            canvas.drawRect(obstacle.x, obstacle.y,
                    obstacle.x + obstacle.width, obstacle.y + obstacle.height, paint);
        }

        // Отрисовка счета
        paint.setColor(Color.BLACK);
        paint.setTextSize(36);
        canvas.drawText("" + score, getWidth() - 100, 50, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isGameRunning) {
                resetGame();
                return true;
            }
            jump();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void gameOver() {
        isGameRunning = false;
        if (callbacks != null) {
            callbacks.gameOver();
        }
    }

    public void resetGame() {
        obstacles.clear();
        score = 0;
        gameSpeed = 10;
        isGameRunning = true;
        dinoY = getHeight() - DINO_HEIGHT - GROUND_LEVEL;
        isJumping = false;

        if (callbacks != null) {
            callbacks.updateScore(score);
            callbacks.hideGameOver();
        }
        handler.post(gameLoop);
    }

    public void pauseGame() {
        isGameRunning = false;
        handler.removeCallbacks(gameLoop);
    }

    public void resumeGame() {
        if (!isGameRunning) {
            isGameRunning = true;
            handler.post(gameLoop);
        }
    }

    private class Obstacle {
        int x, y;
        int width, height;

        Obstacle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
