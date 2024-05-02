package es.uc3m.android.bloom;

import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
public class NotificationDetails extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_fragment);

        // Retrieve and display stored notifications
        SharedPreferences sharedPreferences = getSharedPreferences("Notifications", Context.MODE_PRIVATE);
        LinearLayout notificationsLayout = findViewById(R.id.notifications_layout);

        for (int i = 0; i < sharedPreferences.getInt("notificationCount", 0); i++) {
            String notificationText = sharedPreferences.getString("notification_" + i, "");
            TextView textView = new TextView(this);
            textView.setText(notificationText);
            notificationsLayout.addView(textView);
        }
    }
}

