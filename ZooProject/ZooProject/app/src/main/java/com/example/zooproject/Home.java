package com.example.zooproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_FIRST_LAUNCH = "firstLaunch";

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                MusicPlayer.pauseMusic();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Проверяем и запускаем музыку сразу при создании активности
        if (MusicPlayer.isMusicOn(this)) {
            MusicPlayer.startMusic(this);
        }

        // Получаем SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = settings.getBoolean(PREF_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            LinearLayout welcomeContainer = findViewById(R.id.welcomeContainer);
            welcomeContainer.setVisibility(View.VISIBLE);

            FrameLayout swipeLayout = findViewById(R.id.swipela);
            swipeLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeDown() {
                    Intent intent = new Intent(Home.this, Map.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREF_FIRST_LAUNCH, false);
            editor.apply();
        } else {
            Intent intent = new Intent(this, Map.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }

        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
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
        if (screenOffReceiver != null) {
            unregisterReceiver(screenOffReceiver);
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