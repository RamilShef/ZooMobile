package com.example.zooproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;

public class Start extends AppCompatActivity {

    private static final String PREFS_NAME = "StartPrefs";
    private static final String PREF_FIRST_LAUNCH = "firstLaunch";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final long INTERVAL_TWO_MINUTES = 1 * 60 * 1000;

    private ProgressBar progressBar;
    private TextView progressText;
    private TextView titleText;
    private CardView cardView;
    private int progressStatus = 0;
    private Handler progressHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));

        titleText = findViewById(R.id.titleText);
        Button loadButton = findViewById(R.id.loadButton);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        cardView = findViewById(R.id.cardView);

        // Создаем канал уведомлений
        NotificationHelper.createNotificationChannel(this);
        // Проверяем разрешения
        checkAndRequestPermissions();

        applyGradientToText(titleText, "Зверополис");

        // Получаем SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = settings.getBoolean(PREF_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            // Если приложение запущено впервые, показываем кнопку загрузки
            loadButton.setVisibility(Button.VISIBLE);
            cardView.setVisibility(CardView.VISIBLE);

            loadButton.setOnClickListener(v -> startLoading());
        } else {
            // Если приложение уже запускалось, показываем только надпись "Зверополис" на 3 секунды
            loadButton.setVisibility(Button.GONE);
            cardView.setVisibility(CardView.GONE);

            // Задержка 3 секунды, затем переход на страницу Home
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                navigateToHome();
            }, 3000);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(Start.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            } else {
                scheduleDailyNotification();
            }
        } else {
            scheduleDailyNotification();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleDailyNotification();
        }
    }

    private void scheduleDailyNotification() {
        NotificationManager.scheduleNotifications(this);
    }

    private void startLoading() {
        cardView.setVisibility(CardView.GONE);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressText.setVisibility(TextView.VISIBLE);
        progressStatus = 0;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (progressStatus <= 100) {
                    progressBar.setProgress(progressStatus);
                    progressText.setText(progressStatus + "%");
                    progressStatus += 2;
                    progressHandler.postDelayed(this, 50);
                } else {
                    // Сохраняем флаг, что приложение уже запускалось
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(PREF_FIRST_LAUNCH, false);
                    editor.apply();

                    navigateToHome();
                }
            }
        };

        progressHandler.post(runnable);
    }

    private void applyGradientToText(TextView textView, String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new GradientSpan(textView), 0, text.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    private static class GradientSpan extends CharacterStyle implements UpdateAppearance {
        private final TextView textView;

        GradientSpan(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void updateDrawState(TextPaint paint) {
            float width = textView.getPaint().measureText(textView.getText().toString());
            Shader shader = new LinearGradient(0, 0, width, textView.getTextSize(),
                    new int[]{0xFFF1C604, 0xFF368B0B},
                    null, Shader.TileMode.CLAMP);
            paint.setShader(shader);
        }
    }
}