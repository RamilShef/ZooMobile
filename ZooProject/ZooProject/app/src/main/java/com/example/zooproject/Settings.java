package com.example.zooproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Настроим Switch для управления музыкой
        Switch musicSwitch = findViewById(R.id.musicSwitch);
        musicSwitch.setChecked(MusicPlayer.isMusicOn(this));  // Устанавливаем состояние свитча по сохраненному состоянию

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MusicPlayer.setMusicOn(Settings.this, isChecked);  // Сохраняем состояние свитча
            if (isChecked) {
                MusicPlayer.startMusic(Settings.this);  // Запускаем музыку, если включено
            } else {
                MusicPlayer.stopMusic();  // Останавливаем музыку, если выключено
            }
        });

        // Настроим Switch для управления уведомлениями
        Switch notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationSwitch.setChecked(NotificationManager.areNotificationsOn(this));

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationManager.setNotificationsOn(Settings.this, isChecked);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicPlayer.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicPlayer.isMusicOn(this)) {
            MusicPlayer.startMusic(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) { // Останавливаем только если активность уничтожается
            MusicPlayer.stopMusic();
        }
    }

    public void onMapClick(View view) {
        Intent intent = new Intent(this, Map.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onSearchClick(View view) {
        Intent intent = new Intent(this, Search.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onFeedbackClick(View view) {
        Intent intent = new Intent(this, Feedback.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, Settings.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}