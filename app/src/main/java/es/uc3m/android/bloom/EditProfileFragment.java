package es.uc3m.android.bloom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment implements View.OnClickListener {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;

    TextView name, surname, birthdate, pass;
    Button save, dateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);
        name = view.findViewById(R.id.name_text);
        surname = view.findViewById(R.id.nameField);
        birthdate = view.findViewById(R.id.birth_date);
        pass = view.findViewById(R.id.passwordField);
        save = view.findViewById(R.id.save_button);
        save.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        dateButton = view.findViewById(R.id.birth_date_button);
        dateButton.setOnClickListener(this);
        currentUser = firebaseAuth.getCurrentUser();


        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String user_name = user.getName();
                        String user_surname = user.getSurname();
                        String user_birth = user.getBirthday();
                        name.setText(user_name);
                        if (user_birth != null) {
                            birthdate.setText(user_birth);

                        }
                        surname.setText(user_surname);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_button) {
            String newName = name.getText().toString();
            String newSurname = surname.getText().toString();
            String newBirthdate = birthdate.getText().toString();
            String password = pass.getText().toString();

            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                if (!password.isEmpty()){
                    updatePassword(password);
                }

                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);


                Map<String, Object> updates = new HashMap<>();
                if(!newName.isEmpty()){
                    updates.put("name", newName);

                }
                if(!newSurname.isEmpty()){
                    updates.put("surname", newSurname);

                }
                if(!newBirthdate.isEmpty()){
                    updates.put("birthday", newBirthdate);

                }

                databaseReference.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        Fragment profileFragment = new ProfileFragment();
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nav_fragment, profileFragment)
                                .commit();

                    } else {
                        Toast.makeText(getContext(), "Failed to Update Profile", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.birth_date_button) {
            DatePickerUtil.showDatePicker(getActivity(), birthdate);

        }
    }


    public void updatePassword(String newPassword) {
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            System.out.println("Password updated successfully!");
                        } else {
                           System.err.println("Failed to update password: " + task.getException().getMessage());
                        }
                    });
        } else {
            System.err.println("No user is currently signed in.");
        }
    }


}
