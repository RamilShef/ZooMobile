package com.example.zooproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

public class AnimalSection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sections);


        TextView sectionNameTextView = findViewById(R.id.sectionNameTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);

        Intent intent = getIntent();
        sectionNameTextView.setText(intent.getStringExtra("sectionName"));
        //descriptionTextView.setText(intent.getStringExtra("sectionDescription"));
        String description = intent.getStringExtra("sectionDescription");
        descriptionTextView.setText("\u3000" + description);
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

    public void onBackClick(View view) {
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