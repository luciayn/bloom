package es.uc3m.android.bloom;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment implements View.OnClickListener {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;

    TextView name, surname, birthdate, pass;
    Button save, dateButton;

    ShapeableImageView profilepic;


    private static final int pic_id = 123;
    private static final int gallery_id = 456;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);
        name = view.findViewById(R.id.name_text);
        surname = view.findViewById(R.id.nameField);
        birthdate = view.findViewById(R.id.birth_date);
        pass = view.findViewById(R.id.passwordField);
        save = view.findViewById(R.id.save_button);
        save.setOnClickListener(this);
        dateButton = view.findViewById(R.id.birth_date_button);
        dateButton.setOnClickListener(this);
        profilepic = view.findViewById(R.id.logo); // Make sure this is the ID of your ShapeableImageView in the layout
        profilepic.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile/" + userId + ".jpg");

            // Load the profile image from Firebase Storage
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(getContext()).load(uri.toString()).into(profilepic);
            }).addOnFailureListener(e -> {
                // Log error or show default image if no profile picture is found
            });

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        name.setText(user.getName());
                        surname.setText(user.getSurname());
                        birthdate.setText(user.getBirthday() != null ? user.getBirthday() : "Not set");
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
                if (!password.isEmpty()) {
                    updatePassword(password);
                }

                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);


                Map<String, Object> updates = new HashMap<>();
                if (!newName.isEmpty()) {
                    updates.put("name", newName);

                }
                if (!newSurname.isEmpty()) {
                    updates.put("surname", newSurname);

                }
                if (!newBirthdate.isEmpty()) {
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

        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle("Choose an Action")
                    .setMessage("Please select an option:")
                    .setPositiveButton("Open Camera", (dialog, which) -> {
                        // Open the camera
                        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camera_intent, pic_id);
                    })
                    .setNegativeButton("Upload from Galery", (dialog, which) -> {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, gallery_id);
                    })
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String profileImagePath = "profile/" + userId + ".jpg"; // Use userId for the image file name

        if (requestCode == pic_id && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            StorageReference imageRef = storageRef.child(profileImagePath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(imageData);
            uploadTask.addOnFailureListener(exception -> {
                Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(getContext())
                            .load(uri.toString())
                            .into(profilepic);
                    Toast.makeText(getContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                });
            });

        } else if (requestCode == gallery_id && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            StorageReference imageRef = storageRef.child(profileImagePath);

            UploadTask uploadTask = imageRef.putFile(selectedImage);
            uploadTask.addOnFailureListener(exception -> {
                Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(getContext())
                            .load(uri.toString())
                            .into(profilepic);
                    Toast.makeText(getContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }



}
