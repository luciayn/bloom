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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class BottomNavigation extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private static final String CHANNEL_ID = "es.uc3m.android.notifications.notify";
    private static final String CHANNEL_NAME = "My notification channel";

    private static final int NOTIFICATION_ID = 2;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Schedule the daily notification at 22:00:00
        scheduleDailyNotification(this);
    }

    private void displayNotificationContent(String title, String text) {
        TextView notificationTitleTextView = findViewById(R.id.notificationTitleTextView);
        TextView notificationTextTextView = findViewById(R.id.notificationTextTextView);

        notificationTitleTextView.setText(title);
        notificationTextTextView.setText(text);
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, BottomNavigation.class);
        intent.putExtra("notificationTitle", "Heads-up notification");
        intent.putExtra("notificationText", "This is a heads-up notification");
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void scheduleDailyNotification(Context context) {
        Intent intent = new Intent(context, DailyNotificationReceiver.class);
        intent.putExtra("notificationTitle", "Bloom");
        intent.putExtra("notificationText", "Remember to record today's day!");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up alarm manager to trigger at 22:00 every day
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed, schedule for the next day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
            //        alarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public static class DailyNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract notification content from the intent extras
            String title = intent.getStringExtra("notificationTitle");
            String text = intent.getStringExtra("notificationText");

            Intent notificationIntent = new Intent(context, BottomNavigation.class);
            notificationIntent.putExtra("notificationTitle", title);
            notificationIntent.putExtra("notificationText", text);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important for starting activity from BroadcastReceiver
            context.startActivity(notificationIntent);

            NotificationManager notificationManager = getNotificationManager(context);

            // Configure notification using builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            builder.setContentTitle(title);
            builder.setContentText(text);
            builder.setSmallIcon(R.drawable.baseline_dangerous_24);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setFullScreenIntent(null, true); // heads-up
            builder.addAction(R.drawable.baseline_access_time_24, "Start action",
                    getPendingIntent(context)); // action

            // Create a notification channel for devices running Android Oreo and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            // Show the notification
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        int itemId = item.getItemId();
        if (itemId == R.id.profile_item) {
            fragment = new ProfileFragment();
        } else if (itemId == R.id.notification_item) {
            startActivity(new Intent(this, NotificationsFragment.class));
            return true;
            //fragment = new HomeFragment();
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
