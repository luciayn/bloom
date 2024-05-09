package es.uc3m.android.bloom;

import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener {
    Button newEntry;
    private ImageView image;
    private TextView dayComment, textViewDate;
    private FirebaseDatabase database;

    private String dateStr;

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
        image = rootView.findViewById(R.id.image_photo);
        dayComment = rootView.findViewById(R.id.text_comment);
        newEntry = rootView.findViewById(R.id.new_entry);
        newEntry.setOnClickListener(this);
        image.setOnClickListener(this);
        // Fetch last year's entry
        fetchEntry();

        return rootView;
    }

    private void fetchEntry() {
        DatabaseReference databaseRef = database.getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("entries");

        // Format today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateStr = dateFormat.format(Calendar.getInstance().getTime());

        // Attempt to retrieve today's entry
        databaseRef.child(dateStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Entry for today exists
                    Entry entry = parseEntry(dataSnapshot);
                    Log.d("HomeFragment", "Today's entry loaded: " + entry.getText());
                    updateUIWithEntry(entry);
                } else {
                    // No entry for today, try last year
                    fetchLastYearEntry();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("HomeFragment", "Failed to load today's entry.", databaseError.toException());
                dayComment.setText("Error loading entry.");
            }
        });
    }

    private void fetchLastYearEntry() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1); // Set to last year but keep the same month and day

        // Create a date string with last year but today's month and day
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateStr = dateFormat.format(calendar.getTime());

        DatabaseReference entryRef = database.getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("entries")
                .child(dateStr);

        entryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Entry entry = parseEntry(dataSnapshot);
                    Log.d("HomeFragment", "Last year's entry loaded: " + entry.getText());
                    updateUIWithEntry(entry);
                } else {
//                    dayComment.setText("No entry found for this day last year.");
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
            // Create a reference to the Firebase Storage location
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(entry.getImageUrl());

            // Fetch the download URL
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // URI of the image file fetched from Firebase Storage
                    Glide.with(getContext())
                            .load(uri.toString())  // Use the URI for the Glide loading
                            .into(image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Firebase", "Error fetching image", e);
                    // Handle the error, e.g., show a default image or error message
                    image.setImageResource(R.drawable.no_image);
                }
            });
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
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            dateStr = dateFormat.format(calendar.getTime());
            Fragment newEntryFragment = NewEntryFragment.newInstance(dateStr);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment, newEntryFragment)
                    .commit();
        }else{
            Fragment newEntryFragment = EntryFragment.newInstance(dateStr);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment, newEntryFragment)
                    .commit();
        }
    }
}
