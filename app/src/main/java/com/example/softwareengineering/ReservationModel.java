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
    private String guestId;
    private long dateTimeMillis;

    public ReservationModel(int id, String date, String time, int guestCount,
                            String tableName, String status, String specialRequest, String customerName, String guestId) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.guestCount = guestCount;
        this.tableName = tableName;
        this.status = status;
        this.specialRequest = specialRequest;
        this.customerName = customerName;
        this.guestId = guestId;
    }

    public ReservationModel(int id, String date, String time, int guestCount,
                            String tableName, String status, String specialRequest, String customerName, String guestId, long dateTimeMillis) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.guestCount = guestCount;
        this.tableName = tableName;
        this.status = status;
        this.specialRequest = specialRequest;
        this.customerName = customerName;
        this.guestId = guestId;
        this.dateTimeMillis = dateTimeMillis;
    }

    public String getCustomerName() {
        return customerName;
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

    public String getGuestId() { return guestId; }

    public long getDateTimeMillis() {
        return dateTimeMillis;
    }
}
