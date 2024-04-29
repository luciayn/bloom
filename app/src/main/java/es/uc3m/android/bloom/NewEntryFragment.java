package es.uc3m.android.bloom;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewEntryFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_DATE = "entry_date";

    private FirebaseDatabase database;
    private DatabaseReference userEntriesRef;

    EditText journal;
    ImageButton favourite, camera;
    TextView date, done;
    Boolean fav = Boolean.FALSE;

    public static NewEntryFragment newInstance(String entryDate) {
        NewEntryFragment fragment = new NewEntryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, entryDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_entry_fragment, container, false);

        journal = view.findViewById(R.id.et_diary_entry);
        favourite = view.findViewById(R.id.btn_favorite);
        camera = view.findViewById(R.id.btn_camera);
        done = view.findViewById(R.id.tv_done);
        date = view.findViewById(R.id.tv_date);

        favourite.setOnClickListener(this);
        done.setOnClickListener(this);
        camera.setOnClickListener(this);

        database = FirebaseDatabase.getInstance();

        if (getArguments() != null) {
            String entryDate = getArguments().getString(ARG_DATE);
            userEntriesRef = database.getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("entries")
                    .child(entryDate);


            loadEntryData();
            String formattedDate = DateConverter.convertDate(entryDate);
            date.setText(formattedDate);
        }


        return view;
    }

    private void loadEntryData() {
        userEntriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String entryText = dataSnapshot.child("entry_text").getValue(String.class);
                    fav = dataSnapshot.child("is_favorite").getValue(Boolean.class);
                    journal.setText(entryText);
                    favourite.setImageResource(fav ? R.drawable.marcador_marcado : R.drawable.marcador);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load entry: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_done:
                if (journal.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please fill out the entry to save it", Toast.LENGTH_LONG).show();

                } else {
                    saveOrUpdateEntry();

                }
                break;
            case R.id.btn_camera:
                // Add camera functionality here
                break;
            case R.id.btn_favorite:
                toggleFavorite();
                break;
        }
    }

    private void saveOrUpdateEntry() {
        String entryText = journal.getText().toString();
        Map<String, Object> entryData = new HashMap<>();
        entryData.put("entry_text", entryText);
        entryData.put("is_favorite", fav);

        userEntriesRef.setValue(entryData)
                .addOnSuccessListener(aVoid -> navigateHome())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save entry: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void navigateHome() {
        Fragment imageCalendarFragment = new ImageCalendarFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment, imageCalendarFragment)
                .commit();
    }

    private void toggleFavorite() {
        fav = !fav;
        favourite.setImageResource(fav ? R.drawable.marcador_marcado : R.drawable.marcador);
    }
}
