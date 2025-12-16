package com.example.softwareengineering;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserSignupDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "UserSignup.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "user_signup";
    private static final String COLUMN_USER_ID = "userId";
    private static final String COLUMN_SIGNUP_TIME = "signupTime";
    private static final String COLUMN_PROFILE_IMAGE = "profileImagePath";
    private static final String COLUMN_PROFILE_GENDER = "profileGender";

    public UserSignupDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                COLUMN_SIGNUP_TIME + " LONG, " +
                COLUMN_PROFILE_IMAGE + " TEXT, " +
                COLUMN_PROFILE_GENDER + " TEXT)";
        db.execSQL(query);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void saveSignupTime(String userId, long signupTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_SIGNUP_TIME, signupTime);
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public long getSignupTime(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        long signupTime = 0;
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_SIGNUP_TIME},
                COLUMN_USER_ID + "=?",
                new String[]{userId},
                null, null, null);
        if (cursor.moveToFirst()) {
            signupTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SIGNUP_TIME));
        }
        cursor.close();
        db.close();
        return signupTime;
    }

    public void updateProfileImage(String userId, String imagePath) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_PROFILE_IMAGE, imagePath);

        db.insertWithOnConflict(
                TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        db.close();
    }


    public void updateProfileGender(String userId, String gender) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_GENDER, gender);
        db.update(TABLE_NAME, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }

    public String getProfileImagePath(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        String path = null;

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_PROFILE_IMAGE},
                COLUMN_USER_ID + "=?",
                new String[]{userId},
                null, null, null);

        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE));
        }

        cursor.close();
        db.close();
        return path;
    }

    public String getProfileGender(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        String gender = "boy"; // default

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_PROFILE_GENDER},
                COLUMN_USER_ID + "=?",
                new String[]{userId},
                null, null, null);

        if (cursor.moveToFirst()) {
            String g = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_GENDER));
            if (g != null) gender = g;
        }

        cursor.close();
        db.close();
        return gender;
    }


}

