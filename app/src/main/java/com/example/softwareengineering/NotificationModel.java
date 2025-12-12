package com.example.softwareengineering;

public class NotificationModel {

    private final String title;
    private final String message;
    private final long timestamp;
    private boolean unread;
    private final int icon;
    private final int iconColor;
    private final int iconBgColor;

    public NotificationModel(String title, String message, long timestamp, boolean unread, int icon, int iconColor, int iconBgColor) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.unread = unread;
        this.icon = icon;
        this.iconColor = iconColor;
        this.iconBgColor = iconBgColor;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public int getIcon() {
        return icon;
    }

    public int getIconColor() {
        return iconColor;
    }

    public int getIconBgColor() {
        return iconBgColor;
    }

    public boolean isNewReservation() {
        return title.equals("New Reservation");
    }

    public boolean isUpdateReservation() {
        return title.equals("Reservation Updated");
    }

    public boolean isCancelReservation() {
        return title.equals("Reservation Cancelled");
    }

}
