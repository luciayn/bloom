package es.uc3m.android.bloom;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickerUtil {

    public static void showDatePicker(Context context, final TextView dateTextView) {
        final Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentText = dateTextView.getText().toString();

        if (!currentText.equals("00/00/0000")) {
            try {
                Date date = sdf.parse(currentText);
                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {

            String formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
            dateTextView.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("date_picker_header_year", "id", "android"))
                .performClick();
        datePickerDialog.show();

    }
}
