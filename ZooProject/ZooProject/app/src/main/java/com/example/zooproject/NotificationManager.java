package com.example.zooproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class NotificationManager {
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String PREF_NOTIFICATIONS_ON = "notificationsOn";
    private static final long INTERVAL_TWO_MINUTES = 1 * 60 * 1000;

    public static boolean areNotificationsOn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATIONS_ON, true);
    }

    public static void setNotificationsOn(Context context, boolean isOn) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_NOTIFICATIONS_ON, isOn);
        editor.apply();

        if (isOn) {
            scheduleNotifications(context);
        } else {
            cancelNotifications(context);
        }
    }

    public static void scheduleNotifications(Context context) {
        if (!areNotificationsOn(context)) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long firstTriggerAtMillis = System.currentTimeMillis() + INTERVAL_TWO_MINUTES;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerAtMillis,
                    pendingIntent);
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerAtMillis,
                    INTERVAL_TWO_MINUTES,
                    pendingIntent);
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerAtMillis,
                    INTERVAL_TWO_MINUTES,
                    pendingIntent);
        }
    }

    public static void cancelNotifications(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

}