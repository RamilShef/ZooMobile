package com.example.zooproject;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks {
    private static int activityCount = 0;
    private static boolean wasInBackground = true;

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }

    public static boolean isAppInForeground() {
        return activityCount > 0;
    }

    public static boolean isAppInBackground() {
        return !isAppInForeground();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        if (activityCount == 0) {
            wasInBackground = true;
        }
        activityCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            wasInBackground = true;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}