/// FlappyBirdActivity.java
package com.example.myksu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class FlappyBirdActivity extends AppCompatActivity {
    private GameView gameView;
    private TextView scoreText;
    private int score = 0;
    private Handler handler = new Handler();
    private final int TARGET_SCORE = 20;

    int id;
    ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_flappy_bird);

        id = getIntent().getIntExtra("id", 1);
        progressManager = ProgressManager.getInstance();

        initializeViews();
        setupGame();
        // Запуск музыки
        MusicManager.startMusic(this, R.raw.your_music);

        ImageButton helpButton = findViewById(R.id.btnHelp);
        helpButton.setOnClickListener(v -> showHelpDialog());

        ImageButton backButton = findViewById(R.id.navButton);
        backButton.setOnClickListener(v -> finish());

        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        settingsButton.setOnClickListener(v -> showSettingsDialog());
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        gameView = new GameView(this);
        gameContainer.addView(gameView);

        findViewById(R.id.navButton).setOnClickListener(v -> finish());
        findViewById(R.id.btnShuffle).setOnClickListener(v -> resetGame());
        findViewById(R.id.btnHelp).setOnClickListener(v -> showHelpDialog());
    }

    private void setupGame() {
        score = 0;
        updateScore();
        gameView.resetGame();
    }

    private void updateScore() {
        scoreText.setText("Счет: " + score);
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

        progressManager.completeGameBuilding(id);
        progressManager.saveProgress(this);

        ImageButton continueButton = successDialog.findViewById(R.id.dialog_continue);
        continueButton.setOnClickListener(v -> {
            successDialog.dismiss();
            Intent intent = new Intent(FlappyBirdActivity.this, MapActivity.class);
            startActivity(intent);
            finish();
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

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setContentView(R.layout.game_flappy_bird_rule);

        helpDialog.setTitle(null);
        helpDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Window window = helpDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (300 * getResources().getDisplayMetrics().density);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.dimAmount = 0.7f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ImageButton closeButton = helpDialog.findViewById(R.id.dialog_close);
        closeButton.setOnClickListener(v -> helpDialog.dismiss());

        helpDialog.show();
    }

    class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        private SurfaceHolder holder;
        private Thread gameThread;
        private boolean isRunning = false;
        private Paint paint;

        // Game variables - измененные параметры
        private int birdX, birdY;
        private int birdWidth = 250;
        private int birdHeight = 230;
        private int gravity = 4; // Увеличена гравитация (было 2)
        private int velocity = 0;
        private int jumpVelocity = -40; // Увеличен прыжок (было -30)

        private ArrayList<Pipe> pipes;
        private int pipeWidth = 300;
        private int pipeGap = 900; // Увеличено расстояние между трубами (было 700)
        private int pipeSpeed = 25; // Увеличена скорость труб (было 10)
        private int pipeSpawnDelay = 2000; // Немного увеличен интервал между трубами
        private long lastPipeTime = 0;

        private Random random;
        private int screenWidth, screenHeight;

        private Bitmap birdBitmap;
        private Bitmap pipeTopBitmap;
        private Bitmap pipeBottomBitmap;

        public GameView(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);
            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            random = new Random();
            pipes = new ArrayList<>();

            try {
                birdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.minigames_bird);
                birdBitmap = Bitmap.createScaledBitmap(birdBitmap, birdWidth, birdHeight, true);

                pipeTopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.minigames_pipe_top);
                pipeBottomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.minigames_pipe_top);
            } catch (Exception e) {
                birdBitmap = null;
                pipeTopBitmap = null;
                pipeBottomBitmap = null;
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            screenWidth = getWidth();
            screenHeight = getHeight();
            resetGame();
            startGame();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopGame();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                velocity = jumpVelocity;
                return true;
            }
            return super.onTouchEvent(event);
        }

        @Override
        public void run() {
            while (isRunning) {
                update();
                draw();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void update() {
            // Update bird
            velocity += gravity;
            birdY += velocity;

            // Spawn pipes
            if (System.currentTimeMillis() - lastPipeTime > pipeSpawnDelay) {
                int gapStart = random.nextInt(screenHeight - pipeGap);
                pipes.add(new Pipe(screenWidth, 0, pipeWidth, gapStart, true));
                pipes.add(new Pipe(screenWidth, gapStart + pipeGap, pipeWidth, screenHeight - (gapStart + pipeGap), false));
                lastPipeTime = System.currentTimeMillis();
            }

            // Update pipes
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                pipe.x -= pipeSpeed;

                // Check if bird passed pipe
                if (pipe.x + pipe.width < birdX && !pipe.passed) {
                    pipe.passed = true;
                    score++;
                    handler.post(() -> updateScore());

                    if (score >= TARGET_SCORE) {
                        handler.post(() -> showSuccessDialog());
                        stopGame();
                        return;
                    }
                }

                // Remove off-screen pipes
                if (pipe.x + pipe.width < 0) {
                    pipes.remove(i);
                    i--;
                }
            }

            // Check collisions
            boolean collision = false;

            // With top/bottom
            if (birdY < 0 || birdY + birdHeight > screenHeight) {
                collision = true;
            }

            // With pipes
            for (Pipe pipe : pipes) {
                if (birdX + birdWidth > pipe.x && birdX < pipe.x + pipe.width &&
                        birdY + birdHeight > pipe.y && birdY < pipe.y + pipe.height) {
                    collision = true;
                    break;
                }
            }

            if (collision) {
                handler.post(() -> resetGame());
                return;
            }
        }

        private void draw() {
            if (!holder.getSurface().isValid()) return;

            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.parseColor("#3D5562"));

            // Draw bird
            if (birdBitmap != null) {
                canvas.drawBitmap(birdBitmap, birdX, birdY, paint);
            } else {
                paint.setColor(Color.YELLOW);
                canvas.drawRect(birdX, birdY, birdX + birdWidth, birdY + birdHeight, paint);
            }

            // Draw pipes
            for (Pipe pipe : pipes) {
                if (pipe.isTop && pipeTopBitmap != null) {
                    Bitmap scaledPipe = Bitmap.createScaledBitmap(pipeTopBitmap, pipe.width, pipe.height, true);
                    canvas.drawBitmap(scaledPipe, pipe.x, pipe.y, paint);
                } else if (!pipe.isTop && pipeBottomBitmap != null) {
                    Bitmap scaledPipe = Bitmap.createScaledBitmap(pipeBottomBitmap, pipe.width, pipe.height, true);
                    canvas.drawBitmap(scaledPipe, pipe.x, pipe.y, paint);
                } else {
                    paint.setColor(Color.GREEN);
                    canvas.drawRect(pipe.x, pipe.y, pipe.x + pipe.width, pipe.y + pipe.height, paint);
                }
            }

            holder.unlockCanvasAndPost(canvas);
        }

        private void startGame() {
            if (!isRunning) {
                isRunning = true;
                gameThread = new Thread(this);
                gameThread.start();
            }
        }

        private void stopGame() {
            isRunning = false;
            try {
                if (gameThread != null) {
                    gameThread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void resetGame() {
            stopGame();
            birdX = screenWidth / 4;
            birdY = screenHeight / 2;
            velocity = 0;
            pipes.clear();
            lastPipeTime = System.currentTimeMillis();
            score = 0;
            handler.post(() -> updateScore());
            startGame();
        }
    }

    class Pipe {
        int x, y, width, height;
        boolean passed = false;
        boolean isTop;

        public Pipe(int x, int y, int width, int height, boolean isTop) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isTop = isTop;
        }
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