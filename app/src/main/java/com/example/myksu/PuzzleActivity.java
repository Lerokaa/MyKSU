package com.example.myksu;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private int emptyPieceResId = R.drawable.other_empty_piece_bg;
    private ImageButton btnShuffle, btnHint, btnReset, btnHelp;
    private List<Integer> fixedPositions = new ArrayList<>();
    private int draggedFromPosition = -1;
    private boolean draggedFromBoard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzle_main);

        initializeViews();
        setupImageResources();
        initializePuzzle();
        setupButtonListeners();
    }

    private void initializeViews() {
        puzzleGrid = findViewById(R.id.puzzleGrid);
        piecesGrid = findViewById(R.id.piecesGrid);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnHint = findViewById(R.id.btnHint);
        btnReset = findViewById(R.id.btnReset);
        btnHelp = findViewById(R.id.btnHelp);
    }

    private void setupImageResources() {
        originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.minigames_puzzle_image);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Рассчитываем размеры динамически
        int screenWidth = metrics.widthPixels - (2 * 12); // минус padding из XML
        smallPieceSize = screenWidth / 6; // Размер для кусочков в нижней панели
        largePieceSize = screenWidth / puzzleCols; // Размер для кусочков на игровом поле

        // Устанавливаем высоту игрового поля
        FrameLayout puzzleBoard = findViewById(R.id.puzzleBoard);
        puzzleBoard.post(() -> {
            int availableHeight = metrics.heightPixels
                    - puzzleBoard.getTop()
                    - findViewById(R.id.piecesContainer).getHeight()
                    - findViewById(R.id.btnHelp).getHeight()
                    - (3 * 16); // минус margins

            ViewGroup.LayoutParams params = puzzleBoard.getLayoutParams();
            params.height = availableHeight;
            puzzleBoard.setLayoutParams(params);
        });
    }

    private void setupButtonListeners() {
        findViewById(R.id.navButton).setOnClickListener(v -> finish());
        btnShuffle.setOnClickListener(v -> shufflePuzzle());
        btnHint.setOnClickListener(v -> showHint());
        btnReset.setOnClickListener(v -> resetPuzzle());
        btnHelp.setOnClickListener(v -> showHelpDialog());
    }

    private void showHelpDialog() {
        // Create dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.help_dialog);
        dialog.setCancelable(true);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        TextView title = dialog.findViewById(R.id.dialog_title);
        TextView message = dialog.findViewById(R.id.dialog_message);
        Button closeButton = dialog.findViewById(R.id.dialog_close);

        // Set dialog content
        title.setText("Game Help");
        message.setText("How to play:\n\n1. Drag puzzle pieces from the bottom panel to the game board\n" +
                "2. You can move pieces between positions on the board\n" +
                "3. Fixed pieces cannot be moved");

        // Set close button action
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
    }

    private void initializePuzzle() {
        puzzlePieces = splitImage(originalImage);
        originalPieces = new ArrayList<>(puzzlePieces);

        selectFixedPieces();
        createArrowIndicator();
        setupAdapters();
        setupDragAndDrop();
    }

    private void selectFixedPieces() {
        Random random = new Random();
        while (fixedPositions.size() < 3) {
            int pos = random.nextInt(puzzleRows * puzzleCols);
            if (!fixedPositions.contains(pos)) {
                fixedPositions.add(pos);
                puzzlePieces.remove(originalPieces.get(pos));
            }
        }
    }

    private void createArrowIndicator() {
        Bitmap arrowBitmap = createArrowBitmap(smallPieceSize);
        puzzlePieces.add(0, arrowBitmap);
        Collections.shuffle(puzzlePieces.subList(1, puzzlePieces.size()));
    }

    private void setupAdapters() {
        boardAdapter = new PuzzleBoardAdapter(
                this, puzzleRows * puzzleCols, puzzleGrid, puzzleCols,
                largePieceSize, emptyPieceResId, originalPieces);

        for (int pos : fixedPositions) {
            boardAdapter.setFixedPiece(pos, originalPieces.get(pos));
        }

        piecesAdapter = new PuzzlePiecesAdapter(
                this, puzzlePieces, smallPieceSize, emptyPieceResId);

        puzzleGrid.setAdapter(boardAdapter);
        piecesGrid.setAdapter(piecesAdapter);
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
        piecesGrid.setOnItemLongClickListener((parent, view, position, id) ->
                startDragFromPieces(position, view));

        puzzleGrid.setOnItemLongClickListener((parent, view, position, id) ->
                startDragFromBoard(position, view));

        puzzleGrid.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return handleDragEvent(event);
            }
        });
    }

    private boolean startDragFromPieces(int position, View view) {
        if (position == 0) return false;

        Bitmap piece = piecesAdapter.getItem(position);
        if (piece == null) return false;

        currentDraggedBitmap = piece;
        draggedFromPosition = position;
        draggedFromBoard = false;
        startDragOperation(view);
        return true;
    }

    private boolean startDragFromBoard(int position, View view) {
        if (boardAdapter.isPositionFixed(position)) return false;

        Bitmap piece = boardAdapter.getItem(position);
        if (piece == null) return false;

        currentDraggedBitmap = piece;
        draggedFromPosition = position;
        draggedFromBoard = true;
        boardAdapter.setPiece(position, null);
        startDragOperation(view);
        return true;
    }

    private void startDragOperation(View view) {
        dragImageView = createDragImageView(currentDraggedBitmap);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view) {
            @Override
            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                outShadowSize.set(view.getWidth(), view.getHeight());
                outShadowTouchPoint.set(view.getWidth() / 2, view.getHeight() / 2);
            }
        };
        view.startDragAndDrop(null, shadowBuilder, draggedFromPosition, 0);
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

        if (toPosition >= 0 && !boardAdapter.isPositionFixed(toPosition)) {
            if (draggedFromBoard) {
                handleBoardToBoardMove(toPosition);
            } else {
                handlePiecesToBoardMove(toPosition);
            }
        } else {
            returnPieceToSource();
        }
        cleanupDrag(true);
    }

    private void handleBoardToBoardMove(int toPosition) {
        if (boardAdapter.getItem(toPosition) == null) {
            boardAdapter.setPiece(toPosition, currentDraggedBitmap);
        } else {
            boardAdapter.setPiece(draggedFromPosition, currentDraggedBitmap);
        }
    }

    private void handlePiecesToBoardMove(int toPosition) {
        if (boardAdapter.getItem(toPosition) == null) {
            boardAdapter.setPiece(toPosition, currentDraggedBitmap);
            piecesAdapter.removePiece(draggedFromPosition);
            checkPuzzleComplete();
        } else {
            piecesAdapter.addPiece(currentDraggedBitmap, draggedFromPosition);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up bitmaps to prevent memory leaks
        for (Bitmap piece : puzzlePieces) {
            if (piece != null && !piece.isRecycled()) {
                piece.recycle();
            }
        }
        if (originalImage != null && !originalImage.isRecycled()) {
            originalImage.recycle();
        }
    }
}