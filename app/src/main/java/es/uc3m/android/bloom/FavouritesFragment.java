package es.uc3m.android.bloom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class FavouritesFragment extends Fragment {

    private LinearLayout favoritesContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favourites_fragment, container, false);
        favoritesContainer = view.findViewById(R.id.favourites_layout);
        fetchFavorites();
        return view;
    }

    private void fetchFavorites() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        databaseRef.orderByChild("is_favorite").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Data fetched successfully");
                favoritesContainer.removeAllViews();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println("Snapshot data: " + snapshot.toString());
                    String name = snapshot.child("name").getValue(String.class);
                    String surname = snapshot.child("surname").getValue(String.class);
                    String text = snapshot.child("entry_text").getValue(String.class);
                    String imageUrl = snapshot.child("imageURL").getValue(String.class);
                    if (imageUrl != null) {
                        imageUrl = imageUrl.replace("gs://", "https://firebasestorage.googleapis.com/v0/b/");
                    } else {
                        System.out.println("Image URL is null");
                    }
                    System.out.println("Name: " + name + ", Surname: " + surname);
                    addFavoriteToLayout(name, surname, text, imageUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void addFavoriteToLayout(String name, String surname, String text, String imageUrl) {
        View layout = getLayoutInflater().inflate(R.layout.favourite_item, null);

        TextView nameView = layout.findViewById(R.id.textView6);
        TextView textView = layout.findViewById(R.id.textView7);
        ImageView imageView = layout.findViewById(R.id.image_notification);

        nameView.setText(name + " " + surname);
        textView.setText(text);

        if (isAdded() && getActivity() != null) {
            Picasso.get()
                    .load(imageUrl)
                    .error(R.drawable.no_image) // Assuming you have a default error image in your resources
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Image successfully loaded
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Picasso", "Error loading image: " + imageUrl, e);
                        }
                    });
        } else {
            Log.e("FavouritesFragment", "Fragment not attached when trying to load image.");
        }

        favoritesContainer.addView(layout);
    }
}
