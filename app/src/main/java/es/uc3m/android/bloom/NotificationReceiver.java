package es.uc3m.android.bloom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        showNotification(context);
    }

    private void showNotification(Context context) {
        NotificationHelper.showNotification(context, "Notification Title", "Notification Message");
    }
}

