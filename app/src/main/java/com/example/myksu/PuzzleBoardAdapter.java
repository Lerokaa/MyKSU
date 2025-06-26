package com.example.myksu;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import java.util.Arrays;
import java.util.List;

public class PuzzleBoardAdapter extends BaseAdapter {
    private Context context;
    private int count;
    private int columns;
    private int pieceSize;
    private int emptyPieceResId;
    private List<Bitmap> originalPieces;
    private Bitmap[] boardPieces;
    private boolean[] fixedPieces;

    public PuzzleBoardAdapter(Context context, int count, GridView gridView,
                              int columns, int pieceSize, int emptyPieceResId,
                              List<Bitmap> originalPieces) {
        this.context = context;
        this.count = count;
        this.columns = columns;
        this.pieceSize = pieceSize;
        this.emptyPieceResId = emptyPieceResId;
        this.originalPieces = originalPieces;
        this.boardPieces = new Bitmap[count];
        this.fixedPieces = new boolean[count];
    }

    public void setFixedPiece(int position, Bitmap piece) {
        boardPieces[position] = piece;
        fixedPieces[position] = true;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Bitmap getItem(int position) {
        return boardPieces[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (convertView == null) ? new ImageView(context) : (ImageView) convertView;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(pieceSize, pieceSize));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (boardPieces[position] != null) {
            imageView.setImageBitmap(boardPieces[position]);
        } else {
            imageView.setImageResource(emptyPieceResId);
        }
        return imageView;
    }

    public void setPiece(int position, Bitmap piece) {
        if (!fixedPieces[position]) {
            boardPieces[position] = piece;
            notifyDataSetChanged();
        }
    }

    public boolean isPositionFixed(int position) {
        return fixedPieces[position];
    }

    public int getPositionFromCoordinates(int x, int y) {
        int column = x / pieceSize;
        int row = y / pieceSize;
        int position = row * columns + column;

        if (position >= 0 && position < count) {
            return position;
        }
        return -1;
    }

    public boolean isPuzzleComplete() {
        for (int i = 0; i < count; i++) {
            if (boardPieces[i] == null || !boardPieces[i].sameAs(originalPieces.get(i))) {
                if (!fixedPieces[i]) {
                    return false;
                }
            }
        }
        return true;
    }
}