package com.example.zooproject;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NotificationManager.areNotificationsOn(context) &&
                AppLifecycleTracker.isAppInBackground()) {
            showNotification(context);
        }
    }

    private void showNotification(Context context) {
        // Intent для открытия приложения при нажатии на уведомление
        Intent appIntent = new Intent(context, Home.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Зверополис")
                .setContentText("Вы давно не были с нами, животные уже ждут вас!")
                .setPriority(NotificationCompat.PRIORITY_MAX) // MAX для гарантированного heads-up
                .setDefaults(Notification.DEFAULT_ALL) // Звук, вибрация и свет по умолчанию
                .setVibrate(new long[]{0, 500, 250, 500}) // Паттерн вибрации
                .setAutoCancel(true) // Закрывается при нажатии
                .setContentIntent(pendingIntent) // Действие при нажатии
                .setFullScreenIntent(pendingIntent, true); // Важно для heads-up уведомлений

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }
}