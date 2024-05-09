package es.uc3m.android.bloom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private TextView name, surname, birthdate, edit, favs;
    private ShapeableImageView profilePic; // Add this for the profile image
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        name = view.findViewById(R.id.name_text);
        surname = view.findViewById(R.id.nameField);
        birthdate = view.findViewById(R.id.birth_date);
        edit = view.findViewById(R.id.edit_link);
        progressBar = view.findViewById(R.id.progress_bar);
        profilePic = view.findViewById(R.id.logo);
        favs = view.findViewById(R.id.favourites_link);
        favs.setOnClickListener(this);
        edit.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile/" + userId + ".jpg");

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(getContext())
                        .load(uri.toString())
                        .into(profilePic);
            }).addOnFailureListener(exception -> {
                // Handle any errors, such as no profile picture found
            });

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.GONE);

                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        name.setText(user.getName());
                        surname.setText(user.getSurname());
                        if (user.getBirthday() != null) {
                            birthdate.setText(user.getBirthday());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.edit_link) {
            Fragment editProfileFragment = new EditProfileFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment, editProfileFragment)
                    .commit();
        }else{
            Fragment favouritesFragment = new FavouritesFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment, favouritesFragment)
                    .commit();
        }
    }
}
