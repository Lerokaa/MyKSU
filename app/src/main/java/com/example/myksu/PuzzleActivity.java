package com.example.myksu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout piecesLinearLayout;
    private Bitmap currentDraggedBitmap;
    private List<Bitmap> puzzlePieces = new ArrayList<>();
    private List<Bitmap> originalPieces = new ArrayList<>();
    private final int puzzleRows = 4;
    private final int puzzleCols = 3;
    private boolean isPuzzleComplete = false;
    private Dialog successDialog;
    private Bitmap originalImage;
    private int smallPieceSize;
    private int largePieceSize;
    private PuzzleBoardAdapter boardAdapter;
    private ImageView dragImageView;
    private int emptyPieceResId = R.drawable.other_empty_piece_bg;
    private ImageButton btnShuffle, btnHint, btnHelp;
    private List<Integer> fixedPositions = new ArrayList<>();
    private int draggedFromPosition = -1;
    private boolean draggedFromBoard = false;

    ProgressManager progressManager;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzle_main);

        progressManager = ProgressManager.getInstance();

        id = getIntent().getIntExtra("BUILDING_ID", 1);

        try {
            initializeViews();
            setupImageResources();
            initializePuzzle();

            setupButtonListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing puzzle: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PuzzleActivity", "Initialization error", e);
            finish();
        }
        // Запуск музыки
        MusicManager.startMusic(this, R.raw.your_music);
        // Настройка кнопки "Назад"
        ImageButton backButton = findViewById(R.id.navButton);
        backButton.setOnClickListener(v -> finish()); // Закрывает текущую Activity и возвращает на предыдущую

        // Настройка кнопки настроек
        ImageButton settingsButton = findViewById(R.id.btnShuffle);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        // Настройка кнопки достижений
        ImageButton continueButton = findViewById(R.id.btnHint);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(PuzzleActivity.this, AchievementActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        puzzleGrid = findViewById(R.id.puzzleGrid);
        piecesLinearLayout = findViewById(R.id.piecesLinearLayout);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnHint = findViewById(R.id.btnHint);
        btnHelp = findViewById(R.id.btnHelp);

        if (puzzleGrid == null || piecesLinearLayout == null) {
            throw new IllegalStateException("Critical views not found in layout");
        }
    }

    private void setupImageResources() {
        try {
            originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.minigames_puzzle_image);
            if (originalImage == null) {
                throw new IllegalStateException("Failed to load puzzle image");
            }

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // Рассчитываем размер квадратного пазла
            int screenWidth = metrics.widthPixels - (2 * 12); // учитываем padding
            largePieceSize = screenWidth / puzzleCols;
            smallPieceSize = largePieceSize / 2;

            FrameLayout puzzleBoard = findViewById(R.id.puzzleBoard);
            puzzleBoard.post(() -> {
                try {
                    // Высота игрового поля = размер пазла * количество строк
                    int availableHeight = largePieceSize * puzzleRows;

                    // Проверяем, чтобы не выходило за пределы экрана
                    int maxAvailableHeight = metrics.heightPixels
                            - puzzleBoard.getTop()
                            - findViewById(R.id.piecesContainer).getHeight()
                            - (3 * 16); // margins

                    if (availableHeight > maxAvailableHeight) {
                        // Если не влезает, пересчитываем размеры
                        largePieceSize = maxAvailableHeight / puzzleRows;
                        smallPieceSize = largePieceSize / 2;
                        availableHeight = largePieceSize * puzzleRows;
                    }

                    ViewGroup.LayoutParams params = puzzleBoard.getLayoutParams();
                    params.height = availableHeight;
                    puzzleBoard.setLayoutParams(params);
                } catch (Exception e) {
                    Log.e("PuzzleActivity", "Error calculating layout", e);
                }
            });
        } catch (Exception e) {
            Log.e("PuzzleActivity", "Error in setupImageResources", e);
            throw e;
        }
    }

    private void initializePuzzle() {
        puzzlePieces = splitImage(originalImage);
        originalPieces = new ArrayList<>(puzzlePieces);

        // Проверяем, что разделили правильное количество кусочков
        if (puzzlePieces.size() != puzzleRows * puzzleCols) {
            throw new IllegalStateException("Incorrect number of puzzle pieces. Expected: " +
                    (puzzleRows * puzzleCols) + ", got: " + puzzlePieces.size());
        }

        selectFixedPieces();
        createArrowIndicator();
        setupAdapters();
        setupDragAndDrop();
    }


    private void selectFixedPieces() {
        Random random = new Random();
        fixedPositions.clear(); // Очищаем предыдущие фиксированные позиции

        // Выбираем 3 случайные позиции для фиксации
        while (fixedPositions.size() < 3) {
            int pos = random.nextInt(puzzleRows * puzzleCols);
            if (!fixedPositions.contains(pos)) {
                fixedPositions.add(pos);
            }
        }
    }

    private void createArrowIndicator() {
        Bitmap arrowBitmap = createArrowBitmap(smallPieceSize);
        puzzlePieces.add(0, arrowBitmap);
        Collections.shuffle(puzzlePieces.subList(1, puzzlePieces.size()));
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
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalStateException("Invalid bitmap for splitting");
        }

        // Получаем размеры изображения
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Определяем размер квадратной области для разделения
        int size = Math.min(width, height);

        // Создаем квадратный Bitmap из центра изображения
        Bitmap squareBitmap = Bitmap.createBitmap(bitmap,
                (width - size)/2,
                (height - size)/2,
                size, size);

        // Размер каждого фрагмента (одинаковый для ширины и высоты)
        int pieceSize = size / Math.max(puzzleCols, puzzleRows);

        // Разделяем на фрагменты
        for (int row = 0; row < puzzleRows; row++) {
            for (int col = 0; col < puzzleCols; col++) {
                int x = col * pieceSize;
                int y = row * pieceSize;

                // Проверяем, чтобы фрагмент не выходил за границы
                if (x + pieceSize <= size && y + pieceSize <= size) {
                    Bitmap piece = Bitmap.createBitmap(squareBitmap, x, y, pieceSize, pieceSize);
                    pieces.add(piece);
                } else {
                    Log.e("PuzzleActivity", "Skipping piece at row=" + row + ", col=" + col +
                            " - exceeds bitmap bounds");
                }
            }
        }

        if (pieces.size() != puzzleRows * puzzleCols) {
            throw new IllegalStateException("Failed to create all puzzle pieces. Created: " +
                    pieces.size() + ", expected: " + (puzzleRows * puzzleCols));
        }

        return pieces;
    }


    private void setupAdapters() {
        boardAdapter = new PuzzleBoardAdapter(
                this, puzzleRows * puzzleCols, puzzleGrid, puzzleCols,
                largePieceSize, emptyPieceResId, originalPieces);

        // Добавляем фиксированные кусочки на доску
        for (int pos : fixedPositions) {
            boardAdapter.setFixedPiece(pos, originalPieces.get(pos));
        }

        puzzleGrid.setAdapter(boardAdapter);

        // Заполняем нижнюю панель всеми нефиксированными кусочками
        puzzlePieces.clear();
        for (int i = 0; i < originalPieces.size(); i++) {
            if (!fixedPositions.contains(i)) {
                puzzlePieces.add(originalPieces.get(i));
            }
        }

        // Добавляем стрелку в начало
        puzzlePieces.add(0, createArrowBitmap(smallPieceSize));

        updatePiecesLayout();
    }

    private void setupButtonListeners() {
        findViewById(R.id.navButton).setOnClickListener(v -> finish());
        btnShuffle.setOnClickListener(v -> shufflePuzzle());
        btnHint.setOnClickListener(v -> showHint());
        btnHelp.setOnClickListener(v -> showHelpDialog());
    }

    private void showHelpDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.help_dialog);
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

    private void setupDragAndDrop() {
        puzzleGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            // Не позволяем перемещать фиксированные пазлы
            if (boardAdapter.isPositionFixed(position)) return false;

            Bitmap piece = boardAdapter.getItem(position);
            if (piece == null) return false;

            // Не позволяем перемещать правильно размещенные пазлы
            if (piece.sameAs(originalPieces.get(position))) return false;

            currentDraggedBitmap = piece;
            draggedFromPosition = position;
            draggedFromBoard = true;
            boardAdapter.setPiece(position, null);
            startDragOperation(view);
            return true;
        });

        puzzleGrid.setOnDragListener((v, event) -> handleDragEvent(event));
    }

    private boolean handleDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                updateDragImagePosition(event);
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                updateDragImagePosition(event);
                return true;

            case DragEvent.ACTION_DROP:
                handleDropEvent(event);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                if (!event.getResult()) {
                    returnPieceToSource();
                }
                cleanupDrag(event.getResult());
                return true;
        }
        return false;
    }

    private void updateDragImagePosition(DragEvent event) {
        if (dragImageView != null) {
            dragImageView.setX(event.getX() - largePieceSize/2f);
            dragImageView.setY(event.getY() - largePieceSize/2f);
        }
    }


    private void handleDropEvent(DragEvent event) {
        int toPosition = boardAdapter.getPositionFromCoordinates((int) event.getX(), (int) event.getY());

        if (toPosition >= 0) {
            if (boardAdapter.isPositionFixed(toPosition)) {
                returnPieceToSource();
            } else {
                if (draggedFromBoard) {
                    handleBoardToBoardMove(toPosition);
                } else {
                    handlePiecesToBoardMove(toPosition);
                }
            }
        } else {
            // Если пазл сброшен не на игровое поле
            if (draggedFromBoard) {
                // Возвращаем пазл обратно на доску
                boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
            }
        }
        cleanupDrag(true);
    }


    private boolean containsBitmap(List<Bitmap> list, Bitmap bitmap) {
        if (bitmap == null) return false;

        for (Bitmap b : list) {
            if (b != null && b.sameAs(bitmap)) {
                return true;
            }
        }
        return false;
    }


    private void handleBoardToBoardMove(int toPosition) {
        // Если пазл уже на правильном месте - не перемещаем
        if (originalPieces.get(toPosition).sameAs(currentDraggedBitmap)) {
            boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
            return;
        }

        Bitmap targetPiece = boardAdapter.getItem(toPosition);
        if (targetPiece == null) {
            // Просто перемещаем пазл на пустое место
            boardAdapter.setPiece(toPosition, currentDraggedBitmap);
            boardAdapter.setPiece(draggedFromPosition, null);
        } else {
            // Меняем местами пазлы только если оба не на своих местах
            if (!originalPieces.get(toPosition).sameAs(targetPiece) &&
                    !originalPieces.get(draggedFromPosition).sameAs(currentDraggedBitmap)) {

                boardAdapter.setPiece(toPosition, currentDraggedBitmap);
                boardAdapter.setPiece(draggedFromPosition, targetPiece);
            } else {
                // Если один из пазлов на своем месте, возвращаем обратно
                boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
            }
        }
    }

    private void handlePiecesToBoardMove(int toPosition) {
        if (boardAdapter.isPositionFixed(toPosition)) return;

        if (boardAdapter.getItem(toPosition) == null) {
            boardAdapter.setPiece(toPosition, currentDraggedBitmap);

            // Удаляем только если это не стрелка (позиция 0)
            if (draggedFromPosition > 0) {
                puzzlePieces.remove(draggedFromPosition);
            }

            // Если кусочек на своем месте - фиксируем
            if (originalPieces.get(toPosition).sameAs(currentDraggedBitmap)) {
                boardAdapter.setFixedPiece(toPosition, currentDraggedBitmap);
                fixedPositions.add(toPosition);
            }

            updatePiecesLayout();
            checkPuzzleComplete();
        }
    }


    private void updatePiecesLayout() {
        piecesLinearLayout.removeAllViews();
        for (int i = 0; i < puzzlePieces.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(puzzlePieces.get(i));
            imageView.setLayoutParams(new ViewGroup.LayoutParams(smallPieceSize, smallPieceSize));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(8, 0, 8, 0);

            final int position = i;
            imageView.setOnLongClickListener(v -> {
                if (position == 0) return false;
                if (position >= puzzlePieces.size()) return false;

                Bitmap piece = puzzlePieces.get(position);
                if (piece == null) return false;

                currentDraggedBitmap = piece;
                draggedFromPosition = position;
                draggedFromBoard = false;
                startDragOperation(v);
                return true;
            });

            piecesLinearLayout.addView(imageView);
        }
    }

    private void returnPieceToSource() {
        if (draggedFromBoard) {
            // Возвращаем на доску, если в панели уже есть такой кусочек
            if (containsBitmap(puzzlePieces, currentDraggedBitmap)) {
                boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
            } else {
                // Иначе добавляем в панель
                puzzlePieces.add(currentDraggedBitmap);
                updatePiecesLayout();
            }
        }
    }

    private void startDragOperation(View view) {
        // Создаем увеличенное изображение для перетаскивания
        Bitmap enlargedBitmap = Bitmap.createScaledBitmap(currentDraggedBitmap,
                largePieceSize, largePieceSize, true);
        dragImageView = createDragImageView(enlargedBitmap);

        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view) {
            @Override
            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                outShadowSize.set(largePieceSize, largePieceSize);
                outShadowTouchPoint.set(largePieceSize / 2, largePieceSize / 2);
            }

            @Override
            public void onDrawShadow(Canvas canvas) {
                // Рисуем стандартную тень с полупрозрачностью
                Paint paint = new Paint();
                paint.setColor(Color.argb(100, 0, 0, 0));
                canvas.drawRect(0, 0, largePieceSize, largePieceSize, paint);

                // Рисуем само изображение поверх тени
                canvas.drawBitmap(currentDraggedBitmap, null,
                        new Rect(0, 0, largePieceSize, largePieceSize), null);
            }
        };

        // Начинаем перетаскивание без данных, так как мы управляем всем вручную
        view.startDragAndDrop(null, shadowBuilder, null, 0);
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
        // Собираем все подвижные кусочки с доски
        List<Bitmap> movablePieces = new ArrayList<>();
        for (int i = 0; i < boardAdapter.getCount(); i++) {
            if (!boardAdapter.isPositionFixed(i) && boardAdapter.getItem(i) != null) {
                movablePieces.add(boardAdapter.getItem(i));
                boardAdapter.setPiece(i, null);
            }
        }

        // Добавляем их в нижнюю панель
        puzzlePieces.addAll(movablePieces);
        // Удаляем дубликаты (если такие есть)
        List<Bitmap> uniquePieces = new ArrayList<>();
        for (Bitmap piece : puzzlePieces) {
            if (!containsBitmap(uniquePieces, piece)) {
                uniquePieces.add(piece);
            }
        }
        puzzlePieces = uniquePieces;

        // Перемешиваем все кусочки кроме стрелки
        if (puzzlePieces.size() > 1) {
            Collections.shuffle(puzzlePieces.subList(1, puzzlePieces.size()));
        }

        updatePiecesLayout();
    }

    private void showHint() {
        for (int i = 0; i < boardAdapter.getCount(); i++) {
            if (!boardAdapter.isPositionFixed(i) && boardAdapter.getItem(i) == null) {
                Bitmap correctPiece = originalPieces.get(i);
                for (int j = 1; j < puzzlePieces.size(); j++) {
                    if (puzzlePieces.get(j).sameAs(correctPiece)) {
                        ImageView view = (ImageView) piecesLinearLayout.getChildAt(j);
                        if (view != null) {
                            view.setBackgroundColor(Color.YELLOW);
                            view.postDelayed(() -> {
                                if (view != null) {
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }, 1000);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private void checkPuzzleComplete() {
        if (boardAdapter.isPuzzleComplete() && !isPuzzleComplete) {
            isPuzzleComplete = true;

            // Скрываем нижнюю панель
            findViewById(R.id.piecesContainer).setVisibility(View.GONE);

            // Показываем полное изображение
            showCompleteImage();

            progressManager.completeGameBuilding(id);
            progressManager.saveProgress(this);

            // Показываем диалог через 5 секунд
            new Handler().postDelayed(this::showSuccessDialog, 2000);
        } else if (boardAdapter.isBoardFull() && !boardAdapter.isPuzzleComplete()) {
            Toast.makeText(this, "Не все пазлы на своих местах!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (successDialog != null && successDialog.isShowing()) {
            successDialog.dismiss();
        }
        super.onDestroy();
        if (originalImage != null && !originalImage.isRecycled()) {
            originalImage.recycle();
        }
        for (Bitmap piece : puzzlePieces) {
            if (piece != null && !piece.isRecycled()) {
                piece.recycle();
            }
        }
        for (Bitmap piece : originalPieces) {
            if (piece != null && !piece.isRecycled()) {
                piece.recycle();
            }
        }
    }

    private void showCompleteImage() {
        // Создаем ImageView для полного изображения
        ImageView completeImageView = new ImageView(this);
        completeImageView.setImageBitmap(originalImage);
        completeImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Добавляем его поверх GridView
        FrameLayout puzzleBoard = findViewById(R.id.puzzleBoard);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        puzzleBoard.addView(completeImageView, params);

        // Скрываем GridView
        puzzleGrid.setVisibility(View.GONE);
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
            Intent intent = new Intent(PuzzleActivity.this, MapActivity.class);
            startActivity(intent);
            finish(); // Закрываем текущую активность
        });
        successDialog.show();
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

}