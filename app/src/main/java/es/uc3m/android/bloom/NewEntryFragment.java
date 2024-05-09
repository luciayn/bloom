package es.uc3m.android.bloom;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NewEntryFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_DATE = "entry_date";

    private static final int pic_id = 123;
    private static final int gallery_id = 456;

    private FirebaseDatabase database;
    private DatabaseReference userEntriesRef;

    ImageView dayPicture;

    EditText journal;
    ImageButton favourite, camera;
    TextView date, done;
    Boolean fav = Boolean.FALSE;

    String firebaseImage = "";

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
        dayPicture = view.findViewById(R.id.day_image);
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
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = sdf.format(new Date());
            userEntriesRef = database.getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("entries")
                    .child(currentDate);
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
//                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    // Permission is not granted
//                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//                } else {
//                    // Permission has already been granted
//                    new AlertDialog.Builder(getContext())
//                            .setTitle("Choose an Action")
//                            .setMessage("Please select an option:")
//                            .setPositiveButton("Open Camera", (dialog, which) -> {
//                                // Open the camera
//                                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                startActivityForResult(camera_intent, pic_id);
//                            })
//                            .setNegativeButton("Upload from Galery", (dialog, which) -> {
//                                dialog.dismiss();
//                            })
//                            .show();
//                }

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
        if (!firebaseImage.isEmpty()) {
            entryData.put("imageURL", firebaseImage);
        }

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

    //Method to save the image in tha database
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        if (requestCode == pic_id && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            StorageReference imageRef = storageRef.child("photos/mypicture.jpg");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(imageData);
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads here
            }).addOnSuccessListener(taskSnapshot -> {
                // Here we use the reference to build the gs:// URL directly
                StorageReference ref = taskSnapshot.getMetadata().getReference();
                String gsUrl = String.format("gs://%s/%s", ref.getBucket(), ref.getPath());

                // Correct the path by removing the first "/o/" which is part of the API structure
                gsUrl = gsUrl.replace("/o/", "/");

                // Assuming 'dayPicture' is your ImageView and you want to load the image
                // Note: Glide does not natively support gs:// URLs, you would need to use the FirebaseImageLoader or download the image yourself
                // Here's how you would typically set it for demonstration:
                firebaseImage = gsUrl;

                // If you want to display the image using Glide, you would first download the image using Firebase Storage APIs
                File localFile = null;
                try {
                    localFile = File.createTempFile("images", "jpg");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File finalLocalFile = localFile;
                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        Glide.with(getContext())
                                .load(finalLocalFile)
                                .into(dayPicture);
                        dayPicture.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            });

        } else if (requestCode == gallery_id && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            // Get current date to include in the file name
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = formatter.format(now) + ".jpg";

            // Create a reference to 'photos/' + fileName
            StorageReference imageRef = storageRef.child("photos/" + fileName);

            // Upload the file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(selectedImage);

            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads here
            }).addOnSuccessListener(taskSnapshot -> {
                // Here we use the reference to build the gs:// URL directly
                StorageReference ref = taskSnapshot.getMetadata().getReference();
                String gsUrl = String.format("gs://%s/%s", ref.getBucket(), ref.getPath());

                // Correct the path by removing the first "/o/" which is part of the API structure
                gsUrl = gsUrl.replace("/o/", "/");

                // Assuming 'dayPicture' is your ImageView and you want to load the image
                // Note: Glide does not natively support gs:// URLs, you would need to use the FirebaseImageLoader or download the image yourself
                // Here's how you would typically set it for demonstration:
                firebaseImage = gsUrl;

                // If you want to display the image using Glide, you would first download the image using Firebase Storage APIs
                File localFile = null;
                try {
                    localFile = File.createTempFile("images", "jpg");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File finalLocalFile = localFile;
                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        Glide.with(getContext())
                                .load(finalLocalFile)
                                .into(dayPicture);
                        dayPicture.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            });
        }
    }
}
