package es.uc3m.android.bloom;

import android.os.Bundle;
import android.view.MenuItem;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class BottomNavigation extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private static final String CHANNEL_ID_1 = "es.uc3m.android.notifications.notify";
    private static final String CHANNEL_NAME_1 = "My notification channel 1";
    private static final String CHANNEL_ID_2 = "es.uc3m.android.notifications.notify";
    private static final String CHANNEL_NAME_2 = "My notification channel 2";
    private static final int NOTIFICATION_ID_1 = 1;
    private static final int NOTIFICATION_ID_2 = 2;
    private static boolean notificationCreatedDaily = false;
    private static boolean notificationCreatedPast = false;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntentPD;
    private PendingIntent pendingIntentDR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Schedule past day's content
        schedulePastDay(this);
        // Schedule the daily reminder at 22:00:00
        scheduleDailyReminder(this);
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, BottomNavigation.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void scheduleDailyReminder(Context context) {
        Intent intent = new Intent(context, DailyReminderReceiver.class);
        intent.putExtra("notificationTitle", "Remember to record today's day!");
        intent.putExtra("notificationText", "Register your memories :)");
        pendingIntentDR = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up alarm manager to trigger at 22:00 every day
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 22);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed, schedule for the next day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            //alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntentDR);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    alarmManager.INTERVAL_DAY, pendingIntentDR);
        }
    }

    private void schedulePastDay(Context context) {
        Intent intent = new Intent(context, PastDayReceiver.class);
        intent.putExtra("notificationTitle", "See what you did this day last year!");
        intent.putExtra("notificationText", "Are you curious?");
        pendingIntentPD = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up alarm manager to trigger at 9:00:00 every day
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed, schedule for the next day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            //alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntentPD);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    alarmManager.INTERVAL_DAY, pendingIntentPD);
        }
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public static class DailyReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the notification has already been created
            if (notificationCreatedDaily) {
                return;
            }
            // Extract notification content from the intent extras
            String title = intent.getStringExtra("notificationTitle");
            String text = intent.getStringExtra("notificationText");
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timestamp = dateFormat.format(new Date(System.currentTimeMillis()));

            // Create a new NotificationData object
            NotificationData notification = new NotificationData(title, text, timestamp);

            // Write the notification to Firebase Database
            DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
            notificationsRef.push().setValue(notification);

            // Set the flag to true to indicate that the notification has been created
            notificationCreatedDaily = true;

            Intent notificationIntent = new Intent(context, BottomNavigation.class);
            notificationIntent.putExtra("notificationTitle", title);
            notificationIntent.putExtra("notificationText", text);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important for starting activity from BroadcastReceiver
            context.startActivity(notificationIntent);

            NotificationManager notificationManager = getNotificationManager(context);

            // Configure notification using builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_2);
            builder.setContentTitle(title);
            builder.setContentText(text);
            builder.setSmallIcon(R.drawable.logo);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setFullScreenIntent(null, true); // heads-up
            builder.addAction(R.drawable.baseline_access_time_24, "Start action",
                    getPendingIntent(context)); // action

            // Create a notification channel for devices running Android Oreo and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID_2, CHANNEL_NAME_2,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            // Show the notification
            notificationManager.notify(NOTIFICATION_ID_2, builder.build());

        }
    }

    public static class PastDayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the notification has already been created
            if (notificationCreatedPast) {
                return;
            }

            // Extract notification content from the intent extras
            String title = intent.getStringExtra("notificationTitle");
            String text = intent.getStringExtra("notificationText");
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timestamp = dateFormat.format(new Date(System.currentTimeMillis()));

            // Create a new NotificationData object
            NotificationData notification = new NotificationData(title, text, timestamp);

            // Write the notification to Firebase Database
            DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
            notificationsRef.push().setValue(notification);
            // Set the flag to true to indicate that the notification has been created
            notificationCreatedPast = true;

            Intent notificationIntent = new Intent(context, BottomNavigation.class);
            notificationIntent.putExtra("notificationTitle", title);
            notificationIntent.putExtra("notificationText", text);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important for starting activity from BroadcastReceiver
            context.startActivity(notificationIntent);

            NotificationManager notificationManager = getNotificationManager(context);

            // Configure notification using builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_1);
            builder.setContentTitle(title);
            builder.setContentText(text);
            builder.setSmallIcon(R.drawable.logo);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setFullScreenIntent(null, true); // heads-up
            builder.addAction(R.drawable.baseline_access_time_24, "Start action",
                    getPendingIntent(context)); // action

            // Create a notification channel for devices running Android Oreo and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID_1, CHANNEL_NAME_1,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            // Show the notification
            notificationManager.notify(NOTIFICATION_ID_1, builder.build());

        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        int itemId = item.getItemId();
        if (itemId == R.id.profile_item) {
            fragment = new ProfileFragment();
        } else if (itemId == R.id.notification_item) {
            //startActivity(new Intent(this, NotificationsFragment.class));
            //return true;
            fragment = new NotificationsFragment();
        } else if (itemId == R.id.home_item) {
            fragment = new HomeFragment();
        } else {
            //fragment = new CalendarFragment();
            fragment = new ImageCalendarFragment();

        }


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment, fragment)
                .commit();
        return true;
    }
}
