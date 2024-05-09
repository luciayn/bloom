package es.uc3m.android.bloom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageCalendarFragment extends Fragment {

    private GridView photoCalendarGridView;
    private ImageAdapter calendarAdapter;
    private List<CalendarDay> calendarDays;
    private Spinner monthSpinner, yearSpinner;

    Button select;

    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.year_list_fragment, container, false);
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        monthSpinner = view.findViewById(R.id.monthSpinner);
        yearSpinner = view.findViewById(R.id.yearSpinner);
        photoCalendarGridView = view.findViewById(R.id.calendarGridView);
        select = view.findViewById(R.id.select);
        select.setOnClickListener(v -> {
            int month = monthSpinner.getSelectedItemPosition();
            int year = (int) yearSpinner.getSelectedItem();
            updateCalendar(month, year);
        });
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, getYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(yearAdapter.getPosition(currentYear));
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, getMonths(currentMonth));
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(currentMonth);
        calendarDays = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            calendarDays.add(new CalendarDay(i, R.drawable.no_image));
        }

        calendarAdapter = new ImageAdapter(getContext(), calendarDays);
        photoCalendarGridView.setAdapter(calendarAdapter);
        photoCalendarGridView.setOnItemClickListener((parent, view1, position, id) -> {
            CalendarDay day = calendarDays.get(position);
            int month = monthSpinner.getSelectedItemPosition() + 1;
            int year = (int) yearSpinner.getSelectedItem();
            String date = String.format("%d-%02d-%02d", year, month, day.getDayNumber());

            checkEntryAndRedirect(date);
        });
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedYear = (int) yearSpinner.getSelectedItem();
                updateMonthSpinner(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        updateCalendar(currentMonth, currentYear);

        return view;

    }

    public void checkEntryAndRedirect(String selectedDate) {

        DatabaseReference ref = database.getReference("Users").child(currentUser.getUid()).child("entries").child(selectedDate);


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Fragment fragment;
                if (dataSnapshot.exists()) {
                    fragment = EntryFragment.newInstance(selectedDate);
                } else {
                    fragment = NewEntryFragment.newInstance(selectedDate);
                }

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_fragment, fragment)
                        .commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void updateMonthSpinner(int year) {
        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        String[] months = getMonths(year == currentYear ? currentMonth : 11);

        int previousMonthSelected = monthSpinner.getSelectedItemPosition();
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        if (previousMonthSelected <= currentMonth || year != currentYear) {
            monthSpinner.setSelection(Math.min(previousMonthSelected, months.length - 1));
        }
    }


    private String[] getMonths(int lastMonthIndex) {
        String[] allMonths = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return Arrays.copyOfRange(allMonths, 0, lastMonthIndex + 1);
    }

    private Integer[] getYears() {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        Integer[] years = new Integer[10];
        for (int i = 0; i < 10; i++) {
            years[i] = currentYear - 9 + i;
        }
        return years;
    }


    private void updateCalendar(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        int numDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        DatabaseReference ref = database.getReference("Users").child(currentUser.getUid()).child("entries");


        if (calendarDays != null) {
            calendarDays.clear();
        }


        final int[] numDaysProcessed = new int[1];
        Map<Integer, CalendarDay> dayMap = new HashMap<>();

        for (int i = 1; i <= numDays; i++) {
            final int currentDay = i;
            String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, i);
            DatabaseReference dayRef = ref.child(formattedDate);
            dayRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String imageUrl = dataSnapshot.child("imageURL").getValue(String.class);
                    CalendarDay day;
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        day = new CalendarDay(currentDay, imageUrl);
                    } else {
                        day = new CalendarDay(currentDay, R.drawable.no_image);
                    }

                    dayMap.put(currentDay, day);
                    numDaysProcessed[0]++;
                    if (numDaysProcessed[0] == numDays) {
                        for (int j = 1; j <= numDays; j++) {
                            if (dayMap.containsKey(j)) {
                                calendarDays.add(dayMap.get(j));
                            }
                        }
                        calendarAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Calendar updated successfully", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("Firebase", "Failed to read imageURL.", databaseError.toException());
                    numDaysProcessed[0]++;
                    if (numDaysProcessed[0] == numDays) {
                        for (int j = 1; j <= numDays; j++) {
                            if (dayMap.containsKey(j)) {
                                calendarDays.add(dayMap.get(j));
                            }
                        }
                        calendarAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Calendar update failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


        calendarAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Filter was successful", Toast.LENGTH_LONG).show();

    }


}
