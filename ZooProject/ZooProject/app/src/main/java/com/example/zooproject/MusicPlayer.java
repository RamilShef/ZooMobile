package com.example.zooproject;

import android.content.Context;
import android.media.MediaPlayer;
import android.content.SharedPreferences;

public class MusicPlayer {
    private static MediaPlayer mediaPlayer;
    private static boolean isMusicOn = true; // По умолчанию музыка включена

    public static void startMusic(Context context) {
        if (!isMusicOn) return; // Если музыка выключена в настройках

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.music);
            if (mediaPlayer == null) return;
            mediaPlayer.setLooping(true);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public static void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static boolean isMusicOn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        return preferences.getBoolean("isMusicOn", true);
    }

    public static void setMusicOn(Context context, boolean isOn) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isMusicOn", isOn);
        editor.apply();

        isMusicOn = isOn; // Обновляем состояние

        if (isOn) {
            startMusic(context);
        } else {
            stopMusic();
        }
    }
}