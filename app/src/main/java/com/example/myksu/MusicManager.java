package com.example.myksu;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicManager {
    private static MediaPlayer mediaPlayer;
    private static boolean isMusicOn = true;

    public static void startMusic(Context context, int musicResource) {
        if (!isMusicOn) return;

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(context, musicResource);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public static void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void resumeMusic() {
        if (mediaPlayer != null && isMusicOn) {
            mediaPlayer.start();
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void setMusicState(boolean state) {
        isMusicOn = state;
        if (!state && mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static boolean isMusicOn() {
        return isMusicOn;
    }
}