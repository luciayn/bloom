package es.uc3m.android.bloom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import es.uc3m.android.bloom.NotificationData;
import es.uc3m.android.bloom.R;

public class NotificationsView extends RecyclerView.Adapter<NotificationsView.NotificationViewHolder> {

    private List<NotificationData> notifications;

    public void setNotifications(List<NotificationData> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationData notification = notifications.get(position);
        holder.titleTextView.setText(notification.getTitle());
        holder.textTextView.setText(notification.getText());
        // You can format the timestamp as needed
        holder.timestampTextView.setText(String.valueOf(notification.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, textTextView, timestampTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            textTextView = itemView.findViewById(R.id.text_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }
}

