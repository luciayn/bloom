package es.uc3m.android.bloom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener {
    Button newEntry;
    private ImageView dayPicture;
    private TextView dayComment, textViewDate;
    private FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        // Database and Firebase Authentication initialization
        database = FirebaseDatabase.getInstance();

        // Setup the date TextView to show current date
        textViewDate = rootView.findViewById(R.id.text_date);
        Date currentDate = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        textViewDate.setText(formattedDate);

        // Setup ImageView and TextView for the entry
        dayPicture = rootView.findViewById(R.id.image_photo);
        dayComment = rootView.findViewById(R.id.text_comment);
        newEntry = rootView.findViewById(R.id.new_entry);
        newEntry.setOnClickListener(this);

        // Fetch last year's entry
        fetchLastYearEntry();

        return rootView;
    }

    private void fetchLastYearEntry() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); // January = 0
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to last year but keep the same month and day
        calendar.add(Calendar.YEAR, -1);

        // Create a date string with last year but today's month and day
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String lastYearDate = dateFormat.format(calendar.getTime());

        DatabaseReference entryRef = database.getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("entries")
                .child(lastYearDate);

        entryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Entry entry = parseEntry(dataSnapshot);
                    Log.d("HomeFragment", "Entry loaded: " + entry.getText()); // Confirm data is loaded
                    updateUIWithEntry(entry);
                } else {
                    dayComment.setText("No entry found for this day last year.");
                    Log.d("HomeFragment", "No entry found for last year's same day");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("HomeFragment", "Failed to load last year's entry.", databaseError.toException());
                dayComment.setText("Error loading entry.");
            }
        });
    }


    private Entry parseEntry(DataSnapshot dataSnapshot) {
        String text = dataSnapshot.child("text").getValue(String.class);
        String imageUrl = dataSnapshot.child("imageURL").getValue(String.class);
        return new Entry(text, imageUrl);
    }

    private void updateUIWithEntry(Entry entry) {
        dayComment.setText(entry.getText());
        if (entry.getImageUrl() != null) {
            Glide.with(this).load(entry.getImageUrl()).into(dayPicture);
        }
    }

    static class Entry {
        private String text;
        private String imageUrl;

        public Entry(String text, String imageUrl) {
            this.text = text;
            this.imageUrl = imageUrl;
        }

        public String getText() {
            return text;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.new_entry) {
            Fragment newEntryFragment = new NewEntryFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment, newEntryFragment)
                    .commit();
        }
    }
}
