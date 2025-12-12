package com.example.softwareengineering;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reservation.db";
    private static final int DATABASE_VERSION = 9;

    private static final String TABLE_RESERVATION = "reservations";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_GUEST_COUNT = "guestCount";
    private static final String COLUMN_TABLE = "tableName";
    private static final String COLUMN_REQUEST = "specialRequest";
    private static final String COLUMN_CUSTOMER_NAME= "customerName";
    private static final String COLUMN_CUSTOMER_USER_ID = "customerUserId";

    public ReservationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable =
                "CREATE TABLE " + TABLE_RESERVATION + "(" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_DATE + " TEXT," +
                        COLUMN_TIME + " TEXT," +
                        COLUMN_GUEST_COUNT + " INTEGER," +
                        COLUMN_TABLE + " TEXT," +
                        COLUMN_REQUEST + " TEXT," +
                        COLUMN_CUSTOMER_NAME + " TEXT," +
                        COLUMN_CUSTOMER_USER_ID + " TEXT" +
                        ")";

        db.execSQL(createTable);
//        addSampleData(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATION);
        onCreate(db);
    }

    public boolean addReservation(String date, String time, int guests, String request, String table, String customerName, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TIME, time);
        cv.put(COLUMN_GUEST_COUNT, guests);
        cv.put(COLUMN_TABLE, table);
        cv.put(COLUMN_REQUEST, request);
        cv.put(COLUMN_CUSTOMER_NAME, customerName);
        cv.put(COLUMN_CUSTOMER_USER_ID, userId);

        long result = db.insert(TABLE_RESERVATION, null, cv);
        return result != -1;
    }

    public List<ReservationModel> getAllReservations() {
        List<ReservationModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESERVATION, null, null, null, null, null, COLUMN_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));
                String status = calculateStatus(date, time);

                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName"));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow("customerUserId"));

                ReservationModel res = new ReservationModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        date,
                        time,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GUEST_COUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TABLE)),
                        status,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQUEST)),
                        customerName,
                        userId
                );

                list.add(res);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }



    public void deleteReservation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RESERVATION, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public boolean updateReservation(int id, String date, String time, int guests, String request, String table, String customerName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TIME, time);
        cv.put(COLUMN_GUEST_COUNT, guests);
        cv.put(COLUMN_TABLE, table);
        cv.put(COLUMN_REQUEST, request);
        cv.put(COLUMN_CUSTOMER_NAME, customerName);

        int result = db.update(TABLE_RESERVATION, cv, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }


    private String calculateStatus(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy h:mm a", Locale.ENGLISH);

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            String fullString = date + " " + currentYear + " " + time;

            Date reservationDate = sdf.parse(fullString);
            Date now = new Date();

            assert reservationDate != null;
            if (reservationDate.before(now)) {
                return "Past";
            }

            return "Upcoming";

        } catch (Exception e) {
            return "Upcoming";
        }
    }

    public List<ReservationModel> getAllReservationsWithDateTime() {
        List<ReservationModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESERVATION, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));

                int year = Calendar.getInstance().get(Calendar.YEAR);
                String fullDateTime = date + " " + year + " " + time;

                long datetimeMillis = 0;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy h:mm a", Locale.ENGLISH);
                    Date parsed = sdf.parse(fullDateTime);
                    assert parsed != null;
                    datetimeMillis = parsed.getTime();
                } catch (Exception ignored) {}

                String status = calculateStatus(date, time);
                ReservationModel res = new ReservationModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        date,
                        time,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GUEST_COUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TABLE)),
                        status,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQUEST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_USER_ID)),
                        datetimeMillis
                );


                list.add(res);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }




}
