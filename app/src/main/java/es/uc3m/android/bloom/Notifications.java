/*
 * (C) Copyright 2022 Boni Garcia (https://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package es.uc3m.android.bloom;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class Notifications extends AppCompatActivity {
    private static final String CHANNEL_ID = "es.uc3m.android.notifications.notification";
    private static final String CHANNEL_NAME = "My notification channel";

    private NotificationManager notificationManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_fragment);

        // Schedule the daily notification
        scheduleNotification();

    }
    private void scheduleNotification() {
        // Set up the alarm for 10 PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19); // 10 PM
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:666555444"));
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        // The request code (second argument) is a unique identifier for the PendingIntent,
        // which allows you to distinguish between different PendingIntents.
        // The flags (last argument)  determine how the PendingIntent behaves, such as whether
        // it should be created if it doesn't already exist or if it should update any existing
        // PendingIntent with the same request code.
    }

    private void headsUp(View view) {
        Context context = view.getContext();
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // Configure notification using builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle("Bloom");
        builder.setContentText("Remember to register your day!");
        builder.setSmallIcon(R.drawable.logo);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setFullScreenIntent(null, true); // heads-up
        builder.addAction(R.drawable.logo, "Write entry",
                getPendingIntent()); // action

        // Create a notification channel for devices running Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification
        int notificationId = 0;
        notificationManager.notify(notificationId, builder.build());

        // Store notification details
        SharedPreferences sharedPreferences = getSharedPreferences("Notifications", Context.MODE_PRIVATE);
        int notificationCount = sharedPreferences.getInt("notificationCount", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("notification_" + notificationCount, "Bloom - Remember to register your day!");
        editor.putInt("notificationCount", notificationCount + 1);
        editor.apply();
    }

    private void stopNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

}