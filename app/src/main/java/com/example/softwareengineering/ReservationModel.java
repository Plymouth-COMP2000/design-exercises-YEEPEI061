package com.example.softwareengineering;

public class ReservationModel {

    private int id;
    private String date;
    private String time;
    private int guestCount;
    private String tableName;
    private String status;
    private String specialRequest;
    private String customerName;
    private boolean isPlaceholder;

    public ReservationModel(int id, String date, String time, int guestCount,
                            String tableName, String status, String specialRequest) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.guestCount = guestCount;
        this.tableName = tableName;
        this.status = status;
        this.specialRequest = specialRequest;
        this.customerName = ""; // default empty
    }

    public ReservationModel(int id, String date, String time, int guestCount,
                            String tableName, String status, String specialRequest, String customerName) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.guestCount = guestCount;
        this.tableName = tableName;
        this.status = status;
        this.specialRequest = specialRequest;
        this.customerName = customerName;
    }

    // Placeholder constructor
    public ReservationModel(String date, String time, int guestCount,
                            String tableName, String status, boolean isPlaceholder) {
        this.date = date;
        this.time = time;
        this.guestCount = guestCount;
        this.tableName = tableName;
        this.status = status;
        this.isPlaceholder = isPlaceholder;
        this.customerName = "";
    }

    public String getCustomerName() {
        return customerName;
    }


    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public String getTable() {
        return tableName;
    }

    public String getStatus() {
        return status;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }

}
