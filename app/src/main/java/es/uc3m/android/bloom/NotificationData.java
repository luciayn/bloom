package es.uc3m.android.bloom;

public class NotificationData {
    private String title;
    private String text;
    private String timestamp;

    public NotificationData() {
        // Default constructor required for Firebase
    }

    public NotificationData(String title, String text, String timestamp) {
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}