package es.uc3m.android.bloom;

public class CalendarDay {
    private int dayNumber;
    private int imageURL;

    private String firebaseImage;

    public CalendarDay(int dayNumber, int imageResId) {
        this.dayNumber = dayNumber;
        this.imageURL = imageResId;
    }

    public CalendarDay(int dayNumber, String firebaseImage) {
        this.dayNumber = dayNumber;
        this.firebaseImage = firebaseImage;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public int getImageResId() {
        return imageURL;
    }

    public String getFirebaseImage() {
        return firebaseImage;
    }

    public void setFirebaseImage(String firebaseImage) {
        this.firebaseImage = firebaseImage;
    }
}
