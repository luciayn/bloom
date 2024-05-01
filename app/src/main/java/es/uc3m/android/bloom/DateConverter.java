package es.uc3m.android.bloom;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
    public static String convertDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date date = inputFormat.parse(inputDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM, yyyy");

            assert date != null;
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
