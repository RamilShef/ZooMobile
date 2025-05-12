package com.example.zooproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Feedback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView phone = findViewById(R.id.phone);
        ImageView vkIcon = findViewById(R.id.vkIcon);
        ImageView whatsappIcon = findViewById(R.id.whatsappIcon);
        ImageView telegramIcon = findViewById(R.id.telegramIcon);

        phone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+79274353967"));
            startActivity(intent);
        });

        vkIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/stasyawx"));
            startActivity(intent);
        });

        whatsappIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/79274353967"));
            startActivity(intent);
        });

        telegramIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/haawviiix"));
            startActivity(intent);
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