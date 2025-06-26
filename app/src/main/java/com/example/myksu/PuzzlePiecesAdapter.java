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

    public PuzzlePiecesAdapter(Context context, List<Bitmap> pieces, int pieceSize, int emptyPieceResId) {
        this.context = context;
        this.pieces = new ArrayList<>(pieces);
        this.pieceSize = pieceSize;
        this.emptyPieceResId = emptyPieceResId;
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
        ImageView imageView = (convertView == null) ? new ImageView(context) : (ImageView) convertView;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(pieceSize, pieceSize));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(pieces.get(position));
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
        ImageView view = (ImageView) getView(position, null, null);
        view.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
    }
}