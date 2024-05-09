package es.uc3m.android.bloom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EntryFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_DATE = "date";
    Boolean fav = Boolean.FALSE;
    ImageButton favourite;
    TextView dateTextView, edit, journal;

    ImageView image;
    private FirebaseDatabase databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    DatabaseReference dbRef;

    public EntryFragment() {

    }

    public static EntryFragment newInstance(String dateString) {
        EntryFragment fragment = new EntryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, dateString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entry_fragment, container, false);
        dateTextView = view.findViewById(R.id.tv_date);
        favourite = view.findViewById(R.id.btn_favorite);
        edit = view.findViewById(R.id.edit_entry);
        journal = view.findViewById(R.id.et_diary_entry);
        image = view.findViewById(R.id.day_image);

        favourite.setOnClickListener(this);
        edit.setOnClickListener(this);
        String formattedDate = DateConverter.convertDate(getArguments().getString(ARG_DATE));
        dateTextView.setText(formattedDate);


        databaseReference = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        dbRef = databaseReference.getReference("Users")
                .child(currentUser.getUid())
                .child("entries")
                .child(getArguments().getString(ARG_DATE));

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("entry_text")) {
                    String entryText = dataSnapshot.child("entry_text").getValue(String.class);
                    fav = dataSnapshot.child("is_favorite").getValue(Boolean.class);
                    if (dataSnapshot.child("imageURL").exists()) {

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReferenceFromUrl(dataSnapshot.child("imageURL").getValue(String.class));

                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(getContext())
                                .load(uri.toString())
                                .error(R.drawable.no_image)
                                .into(image)).addOnFailureListener(e -> {

                            image.setImageResource(R.drawable.bell);
                        });



                        image.setVisibility(View.VISIBLE);
                    }
                    if (fav) {
                        favourite.setImageResource(R.drawable.marcador_marcado);
                    } else {
                        favourite.setImageResource(R.drawable.marcador);

                    }
                    journal.setText(entryText);
                } else {
                    journal.setText("");
                    Log.d("Firebase", "No entry_text found for this date");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data: " + databaseError.getMessage());
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_favorite) {
            if (fav) {
                fav = Boolean.FALSE;
                favourite.setImageResource(R.drawable.marcador);
            } else {
                fav = Boolean.TRUE;
                favourite.setImageResource(R.drawable.marcador_marcado);

            }


            dbRef.child("is_favorite").setValue(fav)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Update Fav", "Favorite updated successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Update Fav", "Failed to update favorite: " + e.getMessage());
                        fav = !fav;
                        if (fav) {
                            favourite.setImageResource(R.drawable.marcador_marcado);
                        } else {
                            favourite.setImageResource(R.drawable.marcador);
                        }
                    });
        } else if (v.getId() == R.id.edit_entry) {
            Fragment newEntryFragment = NewEntryFragment.newInstance(getArguments().getString(ARG_DATE));

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment, newEntryFragment)
                    .commit();
        }


    }
}
