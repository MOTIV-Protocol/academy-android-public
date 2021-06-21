package com.insomenia.mobile;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;


public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                Log.e("GLOBALAPPLICATION", "Failed to fetch NotificationManager from context.");
                return;
            }
            String channelId = "kakao_push_channel";
            String channelName = "Kakao SDK Push";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            nm.createNotificationChannel(channel);
            Log.d("GLOBALAPPLICATION", "successfully created a notification channel.");
        }
    }
}