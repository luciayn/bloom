package es.uc3m.android.bloom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener {
    Button newEntry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        TextView textViewDate = rootView.findViewById(R.id.text_date);
        Date currentDate = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        textViewDate.setText(formattedDate);

        addContentView(rootView);
        newEntry = rootView.findViewById(R.id.new_entry);
        newEntry.setOnClickListener(this);

        return rootView;
    }

    private void addContentView(View rootView) {
        ConstraintLayout constraintLayout = rootView.findViewById(R.id.constraint_layout);

//        // Create and add photo
//        ImageView imageView = new ImageView(getContext());
//        imageView.setImageResource(R.drawable.photo1);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins(0, 0, 0, 0);
//        imageView.setLayoutParams(layoutParams);
//        constraintLayout.addView(imageView);
//
//        // Create and add comment
//        TextView textViewComment = new TextView(getContext());
//        textViewComment.setText("This is a comment."); // Set the comment text
//        RelativeLayout.LayoutParams layoutParamsComment = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParamsComment.setMargins(0, 0, 0, 0);
//        textViewComment.setLayoutParams(layoutParamsComment);
//        constraintLayout.addView(textViewComment);
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
