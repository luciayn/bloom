package es.uc3m.android.bloom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<CalendarDay> calendarDays;
    private int columnWidth;

    public ImageAdapter(Context c, List<CalendarDay> calendarDays) {
        this.context = c;
        this.calendarDays = calendarDays;
        this.columnWidth = context.getResources().getDisplayMetrics().widthPixels / 3;
    }

    @Override
    public int getCount() {
        return calendarDays.size();
    }

    @Override
    public Object getItem(int position) {
        return calendarDays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ImageView imageView;
        TextView textView;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.calendar_day_layout, parent, false);
            view.setLayoutParams(new GridView.LayoutParams(columnWidth, columnWidth));
        } else {
            view = convertView;
        }

        imageView = view.findViewById(R.id.dayImage);
        textView = view.findViewById(R.id.dayNumber);

        CalendarDay day = calendarDays.get(position);
        if (day.getFirebaseImage() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(day.getFirebaseImage());

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                    .load(uri.toString())
                    .error(R.drawable.no_image)
                    .into(imageView)).addOnFailureListener(e -> {

                imageView.setImageResource(R.drawable.bell);
            });


        } else {
            imageView.setImageResource(day.getImageResId());

        }
        textView.setText(String.valueOf(day.getDayNumber()));

        return view;
    }
}
