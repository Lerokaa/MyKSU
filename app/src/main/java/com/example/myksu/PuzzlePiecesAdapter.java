package com.example.myksu;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

public class PuzzlePiecesAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> pieces;
    private int pieceSize;
    private int emptyPieceResId;
    private int horizontalSpacing = 4; // spacing between pieces in dp

    public PuzzlePiecesAdapter(Context context, List<Bitmap> pieces, int pieceSize, int emptyPieceResId) {
        this.context = context;
        this.pieces = new ArrayList<>(pieces);
        this.pieceSize = pieceSize;
        this.emptyPieceResId = emptyPieceResId;
        this.horizontalSpacing = (int) (4 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getCount() {
        return pieces.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return pieces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    pieceSize, // ширина
                    pieceSize)); // высота - такая же как ширина
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(horizontalSpacing, 0, horizontalSpacing, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        if (position < pieces.size() && pieces.get(position) != null) {
            imageView.setImageBitmap(pieces.get(position));
        } else {
            imageView.setImageResource(emptyPieceResId);
        }

        return imageView;
    }

    public void removePiece(int position) {
        if (position >= 0 && position < pieces.size()) {
            pieces.remove(position);
            notifyDataSetChanged();
        }
    }

    public void addPiece(Bitmap piece) {
        if (piece != null) {
            pieces.add(piece);
            notifyDataSetChanged();
        }
    }

    public void addPiece(Bitmap piece, int index) {
        if (piece != null && index >= 0 && index <= pieces.size()) {
            pieces.add(index, piece);
            notifyDataSetChanged();
        }
    }

    public void highlightPiece(int position) {
        if (position >= 0 && position < pieces.size()) {
            ImageView view = (ImageView) getView(position, null, null);
            view.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            notifyDataSetChanged();
        }
    }

    // Update piece size dynamically when container size changes
    public void updatePieceSize(int newSize) {
        this.pieceSize = newSize;
        notifyDataSetChanged();
    }
}