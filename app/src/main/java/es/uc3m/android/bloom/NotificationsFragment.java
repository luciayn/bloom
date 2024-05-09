package es.uc3m.android.bloom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsView notificationsView;
    private DatabaseReference notificationsRef;
    private ValueEventListener notificationsListener;
    private static final String TAG = "NotificationsFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsView = new NotificationsView();
        recyclerView.setAdapter(notificationsView);

        // Initialize Firebase database reference
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Set up a ValueEventListener to listen for changes in the notifications data
        notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<NotificationData> notifications = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NotificationData notification = snapshot.getValue(NotificationData.class);
                    notifications.add(notification);
                }
                notificationsView.setNotifications(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read notifications data", databaseError.toException());
            }
        };

        // Add the ValueEventListener to the notifications database reference
        notificationsRef.addValueEventListener(notificationsListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the ValueEventListener when the fragment is destroyed to prevent memory leaks
        if (notificationsRef != null && notificationsListener != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }
    }
}

