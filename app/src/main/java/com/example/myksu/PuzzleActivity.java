package com.example.myksu;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PuzzleActivity extends AppCompatActivity {
    private GridView puzzleGrid;
    private Bitmap currentDraggedBitmap;
    private GridView piecesGrid;
    private List<Bitmap> puzzlePieces = new ArrayList<>();
    private List<Bitmap> originalPieces = new ArrayList<>();
    private final int puzzleRows = 5;
    private final int puzzleCols = 3;
    private Bitmap originalImage;
    private int smallPieceSize;
    private int largePieceSize;
    private PuzzlePiecesAdapter piecesAdapter;
    private PuzzleBoardAdapter boardAdapter;
    private ImageView dragImageView;
    private int emptyPieceResId = R.drawable.empty_piece_bg;
    private ImageButton btnShuffle, btnHint, btnReset, btnHelp;
    private List<Integer> fixedPositions = new ArrayList<>();
    private int draggedFromPosition = -1;
    private boolean draggedFromBoard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzle_main);

        puzzleGrid = findViewById(R.id.puzzleGrid);
        piecesGrid = findViewById(R.id.piecesGrid);

        ImageButton navButton = findViewById(R.id.navButton);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnHint = findViewById(R.id.btnHint);
        btnReset = findViewById(R.id.btnReset);
        btnHelp = findViewById(R.id.btnHelp);

        originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.puzzle_image);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        smallPieceSize = metrics.widthPixels / 6;
        largePieceSize = (int) (metrics.widthPixels * 0.6) / puzzleCols;

        initializePuzzle();

        navButton.setOnClickListener(v -> finish());
        btnShuffle.setOnClickListener(v -> shufflePuzzle());
        btnHint.setOnClickListener(v -> showHint());
        btnReset.setOnClickListener(v -> resetPuzzle());
        btnHelp.setOnClickListener(v -> showHelpDialog());

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        settingsButton.setOnClickListener(v -> showSettingsDialog());
    }


    private void showHelpDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.help_dialog);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.dialog_title);
        TextView message = dialog.findViewById(R.id.dialog_message);
        Button closeButton = dialog.findViewById(R.id.dialog_close);

        title.setText("Помощь в игре");
        message.setText("Как играть:\n\n1. Перетаскивайте кусочки пазла с левой панели на игровое поле\n2. Можно перетаскивать пазлы между позициями на поле\n3. Фиксированные пазлы нельзя перемещать");

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void initializePuzzle() {
        puzzlePieces = splitImage(originalImage);
        originalPieces = new ArrayList<>(puzzlePieces);

        // Выбираем 3 случайных позиции для фиксированных пазлов
        Random random = new Random();
        while (fixedPositions.size() < 3) {
            int pos = random.nextInt(puzzleRows * puzzleCols);
            if (!fixedPositions.contains(pos)) {
                fixedPositions.add(pos);
            }
        }

        // Удаляем фиксированные пазлы из левой колонки
        for (int pos : fixedPositions) {
            puzzlePieces.remove(originalPieces.get(pos));
        }

        // Создаем синий квадрат со стрелкой и ставим его первым элементом
        Bitmap arrowBitmap = createArrowBitmap(smallPieceSize);
        puzzlePieces.add(0, arrowBitmap);

        Collections.shuffle(puzzlePieces.subList(1, puzzlePieces.size()));

        boardAdapter = new PuzzleBoardAdapter(
                this, puzzleRows * puzzleCols, puzzleGrid, puzzleCols,
                largePieceSize, emptyPieceResId, originalPieces);

        // Устанавливаем фиксированные пазлы на их правильные позиции
        for (int pos : fixedPositions) {
            boardAdapter.setFixedPiece(pos, originalPieces.get(pos));
        }

        piecesAdapter = new PuzzlePiecesAdapter(
                this, puzzlePieces, smallPieceSize, emptyPieceResId);

        puzzleGrid.setAdapter(boardAdapter);
        piecesGrid.setAdapter(piecesAdapter);

        setupDragAndDrop();
    }

    private Bitmap createArrowBitmap(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawRect(0, 0, size, size, paint);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        Path arrowPath = new Path();
        int padding = size / 4;
        arrowPath.moveTo(size / 2, padding);
        arrowPath.lineTo(padding, size - padding);
        arrowPath.lineTo(size - padding, size - padding);
        arrowPath.close();

        canvas.drawPath(arrowPath, paint);
        return bitmap;
    }

    private List<Bitmap> splitImage(Bitmap bitmap) {
        List<Bitmap> pieces = new ArrayList<>();
        int pieceWidth = bitmap.getWidth() / puzzleCols;
        int pieceHeight = bitmap.getHeight() / puzzleRows;

        for (int row = 0; row < puzzleRows; row++) {
            for (int col = 0; col < puzzleCols; col++) {
                pieces.add(Bitmap.createBitmap(bitmap, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight));
            }
        }
        return pieces;
    }

    private void setupDragAndDrop() {
        piecesGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position == 0) return false; // Не перетаскиваем синий квадрат

            Bitmap piece = piecesAdapter.getItem(position);
            if (piece == null) return false;

            currentDraggedBitmap = piece;
            draggedFromPosition = position;
            draggedFromBoard = false;
            dragImageView = createDragImageView(piece);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view) {
                @Override
                public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                    outShadowSize.set(view.getWidth(), view.getHeight());
                    outShadowTouchPoint.set(view.getWidth() / 2, view.getHeight() / 2);
                }
            };
            view.startDragAndDrop(null, shadowBuilder, position, 0);
            return true;
        });

        puzzleGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            if (boardAdapter.isPositionFixed(position)) return false;

            Bitmap piece = boardAdapter.getItem(position);
            if (piece == null) return false;

            currentDraggedBitmap = piece;
            draggedFromPosition = position;
            draggedFromBoard = true;
            dragImageView = createDragImageView(piece);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view) {
                @Override
                public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                    outShadowSize.set(view.getWidth(), view.getHeight());
                    outShadowTouchPoint.set(view.getWidth() / 2, view.getHeight() / 2);
                }
            };
            view.startDragAndDrop(null, shadowBuilder, position, 0);
            boardAdapter.setPiece(position, null);
            return true;
        });

        puzzleGrid.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (dragImageView != null) {
                        dragImageView.setImageBitmap(currentDraggedBitmap);
                        dragImageView.setX(event.getX() - largePieceSize/2f);
                        dragImageView.setY(event.getY() - largePieceSize/2f);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    if (dragImageView != null) {
                        dragImageView.setX(event.getX() - largePieceSize/2f);
                        dragImageView.setY(event.getY() - largePieceSize/2f);
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    int toPosition = boardAdapter.getPositionFromCoordinates((int) event.getX(), (int) event.getY());

                    if (toPosition >= 0 && !boardAdapter.isPositionFixed(toPosition)) {
                        if (draggedFromBoard) {
                            // Перемещение между позициями на поле
                            if (boardAdapter.getItem(toPosition) == null) {
                                boardAdapter.setPiece(toPosition, currentDraggedBitmap);
                            } else {
                                // Если место занято - возвращаем на исходную позицию
                                boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
                            }
                        } else {
                            // Перемещение из левой колонки на поле
                            if (boardAdapter.getItem(toPosition) == null) {
                                boardAdapter.setPiece(toPosition, currentDraggedBitmap);
                                piecesAdapter.removePiece(draggedFromPosition);
                                checkPuzzleComplete();
                            } else {
                                // Если место занято - возвращаем в левую колонку
                                piecesAdapter.addPiece(currentDraggedBitmap, draggedFromPosition);
                            }
                        }
                    } else {
                        // Если не попали на поле - возвращаем обратно
                        returnPieceToSource();
                    }
                    cleanupDrag(true);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        returnPieceToSource();
                    }
                    cleanupDrag(event.getResult());
                    return true;
            }
            return false;
        });
    }

    private void returnPieceToSource() {
        if (draggedFromBoard) {
            boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
        } else {
            piecesAdapter.addPiece(currentDraggedBitmap, draggedFromPosition);
        }
    }

    private ImageView createDragImageView(Bitmap piece) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(piece);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(largePieceSize, largePieceSize));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((ViewGroup) findViewById(android.R.id.content)).addView(imageView);
        return imageView;
    }

    private void cleanupDrag(boolean result) {
        if (dragImageView != null) {
            ((ViewGroup) findViewById(android.R.id.content)).removeView(dragImageView);
            dragImageView = null;
        }
        draggedFromPosition = -1;
        draggedFromBoard = false;
    }

    private void shufflePuzzle() {
        List<Bitmap> movablePieces = new ArrayList<>();
        for (int i = 0; i < boardAdapter.getCount(); i++) {
            if (!boardAdapter.isPositionFixed(i) && boardAdapter.getItem(i) != null) {
                movablePieces.add(boardAdapter.getItem(i));
                boardAdapter.setPiece(i, null);
            }
        }

        Collections.shuffle(movablePieces);
        for (Bitmap piece : movablePieces) {
            piecesAdapter.addPiece(piece);
        }
    }

    private void showHint() {
        for (int i = 0; i < boardAdapter.getCount(); i++) {
            if (!boardAdapter.isPositionFixed(i) && boardAdapter.getItem(i) == null) {
                Bitmap correctPiece = originalPieces.get(i);
                for (int j = 1; j < piecesAdapter.getCount(); j++) {
                    if (piecesAdapter.getItem(j).sameAs(correctPiece)) {
                        piecesAdapter.highlightPiece(j);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void resetPuzzle() {
        List<Bitmap> piecesToReturn = new ArrayList<>();
        for (int i = 0; i < boardAdapter.getCount(); i++) {
            if (!boardAdapter.isPositionFixed(i) && boardAdapter.getItem(i) != null) {
                piecesToReturn.add(boardAdapter.getItem(i));
                boardAdapter.setPiece(i, null);
            }
        }

        for (Bitmap piece : piecesToReturn) {
            piecesAdapter.addPiece(piece, 1);
        }
    }

    private void checkPuzzleComplete() {
        if (boardAdapter.isPuzzleComplete()) {
            Toast.makeText(this, "Пазл собран!", Toast.LENGTH_LONG).show();
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